/*
 * MIT License
 *
 * Copyright (c) 2024 Yvan Mazy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.transferproxy.api.profile;

public interface ClientInformation {

    @Deprecated(forRemoval = true)
    static ClientInformation create(final String locale,
                                    final byte viewDistance,
                                    final ChatVisibility chatVisibility,
                                    final boolean chatColors,
                                    final byte displayedSkinParts,
                                    final MainHand mainHand,
                                    final boolean enableTextFiltering,
                                    final boolean allowServerListing) {
        return create(locale,
                viewDistance,
                chatVisibility,
                chatColors,
                displayedSkinParts,
                mainHand,
                enableTextFiltering,
                allowServerListing,
                null);
    }

    static ClientInformation create(final String locale,
                                    final byte viewDistance,
                                    final ChatVisibility chatVisibility,
                                    final boolean chatColors,
                                    final byte displayedSkinParts,
                                    final MainHand mainHand,
                                    final boolean enableTextFiltering,
                                    final boolean allowServerListing,
                                    final ParticleStatus particleStatus) {
        return new ClientInformationImpl(locale,
                viewDistance,
                chatVisibility,
                chatColors,
                displayedSkinParts,
                mainHand,
                enableTextFiltering,
                allowServerListing,
                particleStatus);
    }

    String locale();

    byte viewDistance();

    ChatVisibility chatVisibility();

    boolean chatColors();

    byte displayedSkinParts();

    MainHand mainHand();

    boolean enableTextFiltering();

    boolean allowServerListing();

    ParticleStatus particleStatus();

}