/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste.firebase;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mistrx.buildpaste.BuildPasteMod;
import com.mistrx.buildpaste.util.Functions;
import com.mistrx.buildpaste.util.Variables;
import com.mojang.math.Vector3d;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class Firebase {
    public static String buildingID;
    public static List<Object> blockIDs;
    public static List<String> blockData;
    public static JsonObject blockNBT;
    public static Vector3d size;
    public static String uploadDirection;
    public static String apiEndpoint;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String sendStructureData(List<Object> blockIDs, List<String> blockData, String nbt, List<Integer> size, String lookDirection, String buildname) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String responseString = "-";
        try {
            HttpPost request = buildname == "" ? new HttpPost("https://us-central1-buildpastemod.cloudfunctions.net/v1/builds/addbuild") : new HttpPost("https://us-central1-buildpastemod.cloudfunctions.net/v1/builds/addbuild/" + buildname.replaceAll(" ", "%20"));
            request.addHeader("content-type", "application/json");
            StringEntity params = new StringEntity("{\"uuid\": \"" + Variables.uuid + "\",\"blocks\": " + blockIDs + ",\"data\": " + blockData + ", \"size\": " + size + ",\"direction\": \"" + lookDirection + "\",\"nbt\":" + nbt + "}");
            request.setEntity((HttpEntity)params);
            HttpResponse response = httpClient.execute((HttpUriRequest)request);
            responseString = new BasicResponseHandler().handleResponse(response);
            BuildPasteMod.LOGGER.info(responseString);
        }
        catch (Exception exception) {
        }
        finally {
            BuildPasteMod.LOGGER.info("blockdata: " + blockData);
        }
        return responseString;
    }

    public static String getBuilding(String id, boolean setBlocksOnFinish, Vector3d pos, String direction, String pasteModifier) {
        if (Objects.equals(buildingID, id)) {
            BuildPasteMod.LOGGER.info("https request is unnecessary");
            if (setBlocksOnFinish) {
                Functions.pasteCurrentBuilding(pos, direction, pasteModifier, false, false);
            }
            return "success";
        }
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/builds/get/" + id + "?version=" + Minecraft.m_91087_().m_91309_().getVersion().getId().toString() + "&member=" + Variables.memberLevel);
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
                blockNBT = responseJson.has("nbt") ? responseJson.getAsJsonObject("nbt") : new JsonObject();
                Type listType = new TypeToken<List<Integer>>(){}.getType();
                List sizeList = (List)new Gson().fromJson(sizeJsonElement, listType);
                listType = new TypeToken<List<Object>>(){}.getType();
                List blockIDList = (List)new Gson().fromJson(blockIDJsonElement, listType);
                listType = new TypeToken<List<String>>(){}.getType();
                List blockDataList = (List)new Gson().fromJson(blockDataJsonElement, listType);
                if (!Objects.equals(Variables.memberLevel, "free") && responseJson.has("incompatibleBlocks")) {
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
                size = new Vector3d((double)((Integer)sizeList.get(0)).intValue(), (double)((Integer)sizeList.get(1)).intValue(), (double)((Integer)sizeList.get(2)).intValue());
                uploadDirection = responseJson.get("direction").toString().replaceAll("\"", "");
                blockIDs = new ArrayList<Object>(blockIDList);
                blockData = new ArrayList<String>(blockDataList);
                buildingID = id;
                if (setBlocksOnFinish) {
                    BuildPasteMod.LOGGER.info("finished getting data, now pasting the building");
                    Functions.pasteCurrentBuilding(pos, direction, pasteModifier, false, false);
                } else {
                    BuildPasteMod.LOGGER.info("Not pasting blocks");
                }
                return "success";
            }
            if (responseCode == 500) {
                return "error";
            }
            if (responseCode == 404) {
                return "error-404";
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }

    public static Integer connectAccounts(String email) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/users/verify/" + Variables.mcname + "/" + Variables.uuid + "/" + email);
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

    public static Integer connectAccounts() {
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/users/verify/" + Variables.player.m_7755_().getString() + "/" + Variables.uuid);
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

    public static String disconnectAccount(String uuid, Boolean deleteall) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = null;
            obj = deleteall != false ? new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/users/disconnectaccount/" + uuid + "/true") : new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/users/disconnectaccount/" + uuid + "/false");
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

    public static Integer setSelectedBuildingID(String uuid) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/users/setselectedbuild/" + uuid + "/" + Variables.uploadedBuildID);
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

    public static String getSelectedBuildingID() {
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = new URL("https://us-central1-buildpastemod.cloudfunctions.net/v1/users/build/" + Variables.uuid);
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
                Variables.buildID = responseString = response.toString();
                return "success";
            }
            if (responseCode == 404) {
                BuildPasteMod.LOGGER.info("not connected, connecting");
                Variables.player.m_5661_((Component)Component.m_237113_((String)"Account not connected yet, connecting..."), false);
                int responseCode_ = Firebase.connectAccounts();
                if (responseCode_ == 200) {
                    BuildPasteMod.LOGGER.info("Connected");
                    return Firebase.getSelectedBuildingID();
                }
                return "error";
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPlayerMemberLevel(String mcuuid) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL obj = new URL(apiEndpoint + "/users/data/" + mcuuid);
            httpURLConnection = (HttpURLConnection)obj.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                String responseString;
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                Variables.userDataResponseString = responseString = response.toString();
                JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
                if (responseJson.has("memberLevel")) {
                    JsonElement memberLevelJsonElement = responseJson.get("memberLevel");
                    String memberLevel = memberLevelJsonElement.getAsString();
                    BuildPasteMod.LOGGER.info("MemberLevel: " + memberLevel);
                    Variables.memberLevel = memberLevel;
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

    static {
        apiEndpoint = "https://us-central1-buildpastemod.cloudfunctions.net/v1";
    }
}

