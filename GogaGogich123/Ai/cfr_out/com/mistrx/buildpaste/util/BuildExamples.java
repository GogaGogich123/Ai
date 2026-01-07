/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.util;

import java.util.HashMap;
import java.util.Random;

public class BuildExamples {
    private static String buildsString = "5VDCGGmhZimMvKfM18Mv:Mansion/euL6i6JyOwBxUGBxl00Z:OP Chest/aTIJAWI2CT5Hght0472S:Pagoda/qRi3mpfl4inn4juICRJO:Medieval House/BPf86unj1hLCqNSYSe5P:Modern House/Mi2QSVIFOiJaNBRZS5Xm:Elven House/fVwVAHU4U2oeWzF8sDAE:Knight Statue/vVryAoZFkdLSy3isspz3:Mythic Castle/LPm8spGEoFN08w1k7XEF:TNT Cannon/cpeOUuEvSGuwz7cbkulk:Axolotl Flying Machine/HrPTvkEhUuU7eX6r7r7E:PVP Kits/m0VLrSEXLnq1q9qf8HQF:Huge Spruce Tree/VNrWdrrYM79HMM9hCitu:The White House/S3a2GZzPQ55Y3McvD2VJ:Hidden Staircase/3jgwSILXHJdsrowO1jXm:8x8 Piston Door/FjmKbVhxi3vKKLZd7qH1:Melon Farm/7F3KS5VJjPh7xGQGuamW:Medieval Tower/vGO7crH2P78WBwDgnnV4:Medieval Castle/XoA8lYYeK9Kr6676tyoE:Eagle Statue/0f6LOmUjCfzqwNtP3yFR:Mushroom House/6JDo5TIc0Mfk2hjVU8t6:Medieval Tower/vHzSYuNCDsBQBx81nNxh:Red Mushroom House/SHnQ6Oynmptyy2pWefkO:Medieval Castle Base/mEtb11iEfqcFsL5VR8wS:Small Modern House/BN4DuctPMHpEvKOVi8Nd:Lighthouse/CbI19eGVnXYqzqnw2TcT:Honey Farm/0gWbqYd9HSSN5MRMHGnl:Raid Farm/VU4hZkk81V71dcQanOry:Tree Farm/r2XloBvsIn8vyWLpr4Gb:Vanilla Furniture/00DVV0v0rg7sYzThZm1X:Cathedral/TxthJnSp9eo25QPDZBT3:Medieval Tavern/qNqL10pSPJ2PGyuluQRe:Police Station/BWpXXKtXJNrdAbc91THf:Wooden House/CHVxw4xYKLHwZ0si1Ons:Demon Angel/JBsfznHcKxUgfKkwelYJ:Vinyl Store/ezaBn4NdQjSKXstkPCd9:Office Wing/GBDLtU8eRHgOXrgz13th:Modern Cafe/Oz9e4ggkkMKMsk1IqEJt:Wind Water Mill/3nFvCrfUWa3lBe0bBE00:Dock House/5UeYblpEIQSN1wZjr5TE:Pirate Ship/VETFz3xPRSFGdoTgssXB:Huge Castle Build/yGsrGJf76XHfaQFWiLTh:Small Factory/fANDDgxq4tn1PKu2VKy6:Archway";
    private static String[] buildsArray = buildsString.split("/");
    public static HashMap<String, String> buildsMap = BuildExamples.getHashmapFromBuildsString();

    public static String[] getRandomBuild() {
        int rnd = new Random().nextInt(buildsArray.length);
        return buildsArray[rnd].split(":");
    }

    private static HashMap<String, String> getHashmapFromBuildsString() {
        HashMap<String, String> returnMap = new HashMap<String, String>();
        String[] buildsArray = buildsString.split("/");
        for (int i = 0; i < buildsArray.length; ++i) {
            String[] idAndName = buildsArray[i].split(":");
            returnMap.put(idAndName[0], idAndName[1]);
        }
        return returnMap;
    }
}

