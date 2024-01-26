package com.toma09to.misskeymc.api;

import com.toma09to.misskeymc.model.CreateNoteJson;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

public class MisskeyHttpClient {
    private final String host;
    private final boolean useSsl;
    private final String token;
    private final String visibility;
    private final boolean localOnly;
    private final String channelId;

    private final String prefix;
    private final boolean isDebug;

    public MisskeyHttpClient(String host, boolean useSsl, String token, String visibility, boolean localOnly, String channelId, String prefix, boolean isDebug) {
        this.host = host;
        this.useSsl = useSsl;
        this.token = token;
        this.visibility = visibility;
        this.localOnly = localOnly;
        this.channelId = channelId;
        this.prefix = prefix;
        this.isDebug = isDebug;
    }
    public void sendPost(String message) {
        Logger log = Bukkit.getLogger();
        CreateNoteJson body = new CreateNoteJson(token, visibility, null, localOnly, channelId, prefix + message);
        String url = useSsl ? "https://" : "http://" + host + "/api/notes/create";

        // HTTP Client
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.json()))
                .build();

        if (isDebug) {
            log.info("url = " + url);
            log.info("body = " + body);
        }

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warning("Misskey server doesn't send 200 OK");
                log.warning(String.valueOf(response.statusCode()));
                log.warning(response.body());
            }
        } catch (IOException e) {
            log.warning("IOException was thrown");
        } catch (InterruptedException e) {
            log.warning("InterruptedException was thrown");
        }
    }
}
