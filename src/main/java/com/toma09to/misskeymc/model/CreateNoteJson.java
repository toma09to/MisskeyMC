package com.toma09to.misskeymc.model;

import com.google.gson.Gson;

import javax.annotation.Nonnull;

public class CreateNoteJson {
    public String i;
    public String visibility;
    public String cw;
    public boolean localOnly;
    public String channelId;
    public String text;

    public CreateNoteJson(@Nonnull String token, String visibility, String cw, boolean localOnly, String channelId, @Nonnull String text) {
        this.i = token;
        this.visibility = visibility;
        this.cw = cw;
        this.localOnly = localOnly;
        this.channelId = channelId;
        this.text = text;

        // 空文字列のままだとそれも含めてjson化されてパラメーターが不正になる
        if (this.cw == null || this.cw.isEmpty()) {
            this.cw = null;
        }
        if (this.channelId == null || this.channelId.isEmpty()) {
            this.channelId = null;
        }
    }

    public String json() {
        return new Gson().toJson(this);
    }
}
