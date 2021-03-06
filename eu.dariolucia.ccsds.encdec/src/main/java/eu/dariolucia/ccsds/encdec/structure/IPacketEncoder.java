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

package eu.dariolucia.ccsds.encdec.structure;

/**
 * An interface implemented by objects with packet encoding capabilities. The encoding is performed by providing a packet
 * definition ID and a {@link IEncodeResolver} implementation.
 */
public interface IPacketEncoder {

    /**
     * Encode a packet by providing a packet definition ID and a {@link IEncodeResolver} implementation.
     *
     * @param packetDefinitionId the packet definition to use
     * @param resolver the resolver for the values
     * @return the encoded packet as byte[]
     * @throws EncodingException in case of problems when encoding the packet
     */
    byte[] encode(String packetDefinitionId, IEncodeResolver resolver) throws EncodingException;
}
