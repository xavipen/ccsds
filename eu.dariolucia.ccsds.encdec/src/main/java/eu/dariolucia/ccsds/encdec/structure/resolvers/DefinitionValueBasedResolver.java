/*
 *   Copyright (c) 2019 Dario Lucia (https://www.dariolucia.eu)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package eu.dariolucia.ccsds.encdec.structure.resolvers;

import eu.dariolucia.ccsds.encdec.definition.EncodedParameter;
import eu.dariolucia.ccsds.encdec.definition.PacketDefinition;
import eu.dariolucia.ccsds.encdec.structure.IEncodeResolver;
import eu.dariolucia.ccsds.encdec.structure.PathLocation;
import eu.dariolucia.ccsds.encdec.value.BitString;
import eu.dariolucia.ccsds.encdec.value.StringUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A resolver that returns the value as set in the definition of the encoded parameter, if present.
 * If not present, then the call is routed to the delegate resolver.
 * If present but the parsing of the value returns null or an exception, depending on the strategy selected at
 * construction time, an exception can be thrown or the call is routed to the delegate resolver.
 */
public class DefinitionValueBasedResolver implements IEncodeResolver {

    private final IEncodeResolver delegate;

    private final boolean delegateCallOnNullOrException;

    protected PacketDefinition currentDefinition;

    public DefinitionValueBasedResolver(IEncodeResolver delegate, boolean delegateCallOnNullOrException) {
        if(delegate == null) {
            throw new NullPointerException("Delegate resolver must be provided");
        }
        this.delegate = delegate;
        this.delegateCallOnNullOrException = delegateCallOnNullOrException;
    }

    /**
     * Generic function used to retrieve a value of a given type or to forward the call to the delegate object.
     *
     * @param parameter the encoded parameter
     * @param location the location of the encoded parameter
     * @param resultClass the expected class of the value
     * @param delegateFunction the delegate function
     * @param parseFunction the parse function
     * @param <T> the type of the result
     * @return the value object
     */
    private <T> T retrieveValue(EncodedParameter parameter, PathLocation location, Class<T> resultClass,
                                BiFunction<EncodedParameter, PathLocation, T> delegateFunction, Function<String, T> parseFunction) {
        String value = parameter.getValue();
        if(value == null) {
            return delegateFunction.apply(parameter, location);
        } else {
            try {
                T theObj = parseFunction.apply(value);
                if(theObj == null) {
                    if(delegateCallOnNullOrException) {
                        return delegateFunction.apply(parameter, location);
                    } else {
                        throw new RuntimeException("Parsing of " + value + " for parameter location " + location + " and type " + resultClass.getSimpleName() + " returned null");
                    }
                } else {
                    return theObj;
                }
            } catch (Exception e) {
                if(delegateCallOnNullOrException) {
                    return delegateFunction.apply(parameter, location);
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Generic function used to retrieve a value of a given type or to forward the call to the delegate object.
     *
     * @param parameter the encoded parameter
     * @param location the location of the encoded parameter
     * @param length the max length of the value
     * @param resultClass the expected class of the value
     * @param delegateFunction the delegate function
     * @param parseFunction the parse function
     * @param <T> the type of the result
     * @return the value object
     */
    private <T> T retrieveValue(EncodedParameter parameter, PathLocation location, int length, Class<T> resultClass,
                                TriFunction<EncodedParameter, PathLocation, Integer, T> delegateFunction, Function<String, T> parseFunction) {
        String value = parameter.getValue();
        if(value == null) {
            return delegateFunction.apply(parameter, location, length);
        } else {
            try {
                T theObj = parseFunction.apply(value);
                if(theObj == null) {
                    if(delegateCallOnNullOrException) {
                        return delegateFunction.apply(parameter, location, length);
                    } else {
                        throw new RuntimeException("Parsing of " + value + " for parameter location " + location + " and type " + resultClass.getSimpleName() + " returned null");
                    }
                } else {
                    return theObj;
                }
            } catch (Exception e) {
                if(delegateCallOnNullOrException) {
                    return delegateFunction.apply(parameter, location, length);
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public boolean getBooleanValue(EncodedParameter parameter, PathLocation location) {
        return retrieveValue(parameter, location, Boolean.class, delegate::getBooleanValue, Boolean::parseBoolean);
    }

    @Override
    public int getEnumerationValue(EncodedParameter parameter, PathLocation location) {
        return retrieveValue(parameter, location, Integer.class, delegate::getEnumerationValue, Integer::parseInt);
    }

    @Override
    public long getSignedIntegerValue(EncodedParameter parameter, PathLocation location) {
        return retrieveValue(parameter, location, Long.class, delegate::getSignedIntegerValue, Long::parseLong);
    }

    @Override
    public long getUnsignedIntegerValue(EncodedParameter parameter, PathLocation location) {
        return retrieveValue(parameter, location, Long.class, delegate::getUnsignedIntegerValue, Long::parseLong);
    }

    @Override
    public double getRealValue(EncodedParameter parameter, PathLocation location) {
        return retrieveValue(parameter, location, Double.class, delegate::getRealValue, Double::parseDouble);
    }

    @Override
    public Instant getAbsoluteTimeValue(EncodedParameter parameter, PathLocation location) {
        return retrieveValue(parameter, location, Instant.class, delegate::getAbsoluteTimeValue, Instant::parse);
    }

    @Override
    public Duration getRelativeTimeValue(EncodedParameter parameter, PathLocation location) {
        return retrieveValue(parameter, location, Duration.class, delegate::getRelativeTimeValue, Duration::parse);
    }

    @Override
    public BitString getBitStringValue(EncodedParameter parameter, PathLocation location, int maxBitlength) {
        return retrieveValue(parameter, location, maxBitlength, BitString.class, delegate::getBitStringValue, BitString::parseBitString);
    }

    @Override
    public byte[] getOctetStringValue(EncodedParameter parameter, PathLocation location, int maxByteLength) {
        return retrieveValue(parameter, location, maxByteLength, byte[].class, delegate::getOctetStringValue, StringUtil::toByteArray);
    }

    @Override
    public String getCharacterStringValue(EncodedParameter parameter, PathLocation location, int maxStringLength) {
        return retrieveValue(parameter, location, maxStringLength, String.class, delegate::getCharacterStringValue, Function.identity());
    }

    @Override
    public Object getExtensionValue(EncodedParameter parameter, PathLocation location) {
        return delegate.getExtensionValue(parameter, location);
    }

    @Override
    public AbsoluteTimeDescriptor getAbsoluteTimeDescriptor(EncodedParameter parameter, PathLocation location, Instant value) {
        return delegate.getAbsoluteTimeDescriptor(parameter, location, value);
    }

    @Override
    public RelativeTimeDescriptor getRelativeTimeDescriptor(EncodedParameter parameter, PathLocation location, Duration value) {
        return delegate.getRelativeTimeDescriptor(parameter, location, value);
    }

    @Override
    public void startPacketEncoding(PacketDefinition pd) {
        this.currentDefinition = pd;
        delegate.startPacketEncoding(pd);
    }

    @Override
    public void endPacketEncoding() {
        this.currentDefinition = null;
        delegate.endPacketEncoding();
    }

    @FunctionalInterface
    private interface TriFunction<T, U, K, R> {
        R apply(T t, U u, K k);
    }
}