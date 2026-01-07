/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.firebase;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.firebase.BuildDataStore;
import com.mistrx.buildpaste.player.PlayerData;
import com.mistrx.buildpaste.player.PlayerDataManager;
import com.mistrx.buildpaste.util.Variables;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.entity.player.Player;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joml.Vector3d;

public class Firebase {
    public static HashMap<String, BuildDataStore> storedBuilds = new HashMap();
    public static List<String> lastPastedBuilds = new ArrayList<String>();
    public static String apiEndpoint = "https://us-central1-buildpastemod.cloudfunctions.net/v1";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String sendStructureData(Player player, List<Object> blockIDs, List<String> blockData, String nbt, List<Integer> size, String lookDirection, String buildname) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String responseString = "-";
        try {
            HttpPost request = Objects.equals(buildname, "") ? new HttpPost(apiEndpoint + "/builds/addbuild") : new HttpPost(apiEndpoint + "/builds/addbuild/" + buildname.replaceAll(" ", "%20"));
            request.addHeader("content-type", "application/json");
            StringEntity params = new StringEntity("{\"uuid\": \"" + PlayerDataManager.getOrCreatePlayerData(player).getUuid() + "\",\"blocks\": " + String.valueOf(blockIDs) + ",\"data\": " + String.valueOf(blockData) + ", \"size\": " + String.valueOf(size) + ",\"direction\": \"" + lookDirection + "\",\"nbt\":" + nbt + "}");
            request.setEntity((HttpEntity)params);
            HttpResponse response = httpClient.execute((HttpUriRequest)request);
            responseString = new BasicResponseHandler().handleResponse(response);
        }
        catch (Exception exception) {}
        return responseString;
    }

    public static BuildDataStore getBuildData(String id) throws Exception {
        BuildPasteMod.LOGGER.info("Firebase.getBuilding");
        if (lastPastedBuilds.contains(id)) {
            lastPastedBuilds.remove(id);
        }
        lastPastedBuilds.add(id);
        if (storedBuilds.containsKey(id)) {
            BuildPasteMod.LOGGER.info("loading stored build");
            BuildDataStore savedBuild = storedBuilds.get(id);
            return savedBuild;
        }
        HttpURLConnection httpURLConnection = null;
        try {
            BuildPasteMod.LOGGER.info("getting data from api");
            URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/builds/get/" + id + "?version=1.21.8&member=pro");
            httpURLConnection = (HttpURLConnection)obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            BuildPasteMod.LOGGER.info("HTTPS ResponseCode: " + responseCode);
            if (responseCode == 200) {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String responseString = response.toString();
                JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
                JsonElement sizeJsonElement = responseJson.get("size");
                JsonElement blockIDJsonElement = responseJson.get("blocks");
                JsonElement blockDataJsonElement = responseJson.get("data");
                Type listType = new TypeToken<List<Integer>>(){}.getType();
                List sizeList = (List)new Gson().fromJson(sizeJsonElement, listType);
                listType = new TypeToken<List<Object>>(){}.getType();
                List blockIDList = (List)new Gson().fromJson(blockIDJsonElement, listType);
                listType = new TypeToken<List<String>>(){}.getType();
                List blockDataList = (List)new Gson().fromJson(blockDataJsonElement, listType);
                if (responseJson.has("incompatibleBlocks")) {
                    JsonObject incompatibleBlocksObject = responseJson.get("incompatibleBlocks").getAsJsonObject();
                    if (incompatibleBlocksObject.has("amount") && incompatibleBlocksObject.has("exampleBlocks")) {
                        listType = new TypeToken<List<String>>(){}.getType();
                        List exampleBlocks = (List)new Gson().fromJson(incompatibleBlocksObject.get("exampleBlocks"), listType);
                        int incompatibleBlocksAmount = incompatibleBlocksObject.get("amount").getAsInt();
                        Variables.incompatibleBlocksExampleBlocksReplacedArray = exampleBlocks;
                        Variables.incompatibleBlocksAmount = incompatibleBlocksAmount;
                    }
                } else {
                    Variables.incompatibleBlocksExampleBlocksReplacedArray = new ArrayList<String>();
                    Variables.incompatibleBlocksAmount = 0;
                }
                ArrayList<Object> blockIDs = new ArrayList<Object>(blockIDList);
                ArrayList<String> blockData = new ArrayList<String>(blockDataList);
                Vector3d size = new Vector3d((double)((Integer)sizeList.get(0)).intValue(), (double)((Integer)sizeList.get(1)).intValue(), (double)((Integer)sizeList.get(2)).intValue());
                String uploadDirection = responseJson.get("direction").toString().replaceAll("\"", "");
                JsonObject blockNBT = responseJson.has("nbt") ? responseJson.getAsJsonObject("nbt") : new JsonObject();
                BuildDataStore buildDataStore = new BuildDataStore(blockIDs, blockData, blockNBT, size, uploadDirection);
                storedBuilds.put(id, buildDataStore);
                return buildDataStore;
            }
            if (responseCode == 500) {
                throw new Exception("getbuilddata/http-internal-error");
            }
            if (responseCode == 404) {
                throw new Exception("getbuilddata/error-404");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new Exception("getbuilddata/connection-error");
        }
        throw new Exception("getbuilddata/unexpected-error");
    }

    public static BuildDataStore getLocallyStoredBuildData(String id) {
        if (storedBuilds.containsKey(id)) {
            return storedBuilds.get(id);
        }
        return null;
    }

    public static Integer connectAccounts(Player player, String email) {
        HttpURLConnection httpURLConnection = null;
        try {
            PlayerData playerData = PlayerDataManager.getOrCreatePlayerData(player);
            URL obj = new URL(apiEndpoint + "/users/verify/" + playerData.getMcname() + "/" + playerData.getUuid() + "/" + email);
            httpURLConnection = (HttpURLConnection)obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String string = response.toString();
            }
            return responseCode;
        }
        catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static Integer connectAccounts(Player player) {
        PlayerData playerData = PlayerDataManager.getOrCreatePlayerData(player);
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = new URL(apiEndpoint + "/users/verify/" + playerData.getMcname() + "/" + playerData.getUuid());
            httpURLConnection = (HttpURLConnection)obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String string = response.toString();
            }
            return responseCode;
        }
        catch (IOException e) {
            e.printStackTrace();
            return 500;
        }
    }

    public static String disconnectAccount(String uuid, Boolean deleteall) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = null;
            obj = deleteall != false ? new URL(apiEndpoint + "/users/disconnectaccount/" + uuid + "/true") : new URL(apiEndpoint + "/users/disconnectaccount/" + uuid + "/false");
            httpURLConnection = (HttpURLConnection)obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String responseString = response.toString();
                return responseString;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "last return reached, probably error";
    }

    public static Integer setSelectedBuildingID(String uuid, String id) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = new URL(apiEndpoint + "/users/setselectedbuild/" + uuid + "/" + id);
            httpURLConnection = (HttpURLConnection)obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 404) {
                return 0;
            }
            return 1;
        }
        catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getSelectedBuildingID(Player player) throws Exception {
        PlayerData playerData = PlayerDataManager.getOrCreatePlayerData(player);
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = new URL(apiEndpoint + "/users/build/" + playerData.getUuid());
            httpURLConnection = (HttpURLConnection)obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            String responseString = "";
            if (responseCode == 200) {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                responseString = response.toString();
                PlayerDataManager.getOrCreatePlayerData(player).setSelectedBuildId(responseString);
                return responseString;
            }
            if (responseCode == 404) {
                BuildPasteMod.LOGGER.info("not connected, connecting");
                int responseCode_ = Firebase.connectAccounts(player);
                if (responseCode_ == 200) {
                    BuildPasteMod.LOGGER.info("Connected");
                    return Firebase.getSelectedBuildingID(player);
                }
                if (responseCode_ == 500) {
                    throw new Exception("getbuildid/unexpected-internal-error");
                }
                throw new Exception("getbuildid/no-account");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IOException("getbuildid/ioexception");
        }
        throw new Exception("getbuildid/unexpected-error");
    }

    public static String getBuildName(String id) {
        BuildPasteMod.LOGGER.info("getting getBuildName");
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = new URL(apiEndpoint + "/_api/build/" + id);
            httpURLConnection = (HttpURLConnection)obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String responseString = response.toString();
                JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
                if (responseJson.has("name")) {
                    JsonElement nameJsonElement = responseJson.get("name");
                    return nameJsonElement.getAsString();
                }
            }
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getPlayerMemberLevel(String mcuuid) {
        BuildPasteMod.LOGGER.info("getting memberLevel");
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = new URL(apiEndpoint + "/users/data/" + mcuuid);
            httpURLConnection = (HttpURLConnection)obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String responseString = response.toString();
                JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
                if (responseJson.has("memberLevel")) {
                    JsonElement memberLevelJsonElement = responseJson.get("memberLevel");
                    String memberLevel = memberLevelJsonElement.getAsString();
                    BuildPasteMod.LOGGER.info("MemberLevel of User: " + memberLevel);
                    return memberLevel;
                }
            }
            return "free";
        }
        catch (IOException e) {
            e.printStackTrace();
            return "free";
        }
    }
}

