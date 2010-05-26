package com.gamaray.arex;

import java.util.ArrayList;

import com.gamaray.arex.context.ARXContext;
import com.gamaray.arex.gui.Bitmap;
import com.gamaray.arex.gui.DrawWindow;
import com.gamaray.arex.gui.Drawable;
import com.gamaray.arex.gui.GUIUtil;
import com.gamaray.arex.gui.TextBlock;
import com.gamaray.arex.render3d.Color;

public class ARXMessages {
    public static Bitmap refreshIcon, refreshCompleteIcon, targetIcon, compassIcon, worldIcon, errorIcon, warningIcon,
            downloadIcon, downloadErrorIcon, downloadCompleteIcon;

    private static int count = 0;
    static ArrayList messages = new ArrayList();
    private static ArrayList messagesTmp = new ArrayList();
    public static boolean iconsLoaded = false;

    public static void loadIcons(ARXContext ctx) {
        targetIcon = GUIUtil.loadIcon("target.png", ctx);
        compassIcon = GUIUtil.loadIcon("compass.png", ctx);
        warningIcon = GUIUtil.loadIcon("warning.png", ctx);
        errorIcon = GUIUtil.loadIcon("error.png", ctx);
        refreshIcon = GUIUtil.loadIcon("refresh.png", ctx);
        refreshCompleteIcon = GUIUtil.loadIcon("refreshComplete.png", ctx);
        downloadIcon = GUIUtil.loadIcon("download.png", ctx);
        downloadCompleteIcon = GUIUtil.loadIcon("downloadComplete.png", ctx);
        downloadErrorIcon = GUIUtil.loadIcon("downloadError.png", ctx);
        worldIcon = GUIUtil.loadIcon("world.png", ctx);

        iconsLoaded = true;
    }

    public static void putMessage(String msgId, String msgTxt, Bitmap icon, long duration) {
        putMessage(msgId, msgTxt, "", icon, duration, false);
    }

    public static void putMessage(String msgId, String msgTxt, String miniTxt, Bitmap icon, long duration,
            boolean volatileMsg) {
        boolean updated = false;
        for (int i = 0; i < messages.size(); i++) {
            Message msg = (Message) messages.get(i);

            if (msg.msgId.equals(msgId)) {
                if (msgTxt != null)
                    msg.msgTxt = msgTxt;
                if (miniTxt != null)
                    msg.miniTxt = miniTxt;
                if (icon != null)
                    msg.icon = icon;
                if (duration != -1)
                    msg.duration = duration;
                if (duration != -1)
                    msg.expires = System.currentTimeMillis() + msg.duration;

                updated = true;
            }
        }

        if (!updated) {
            Message newMsg = (volatileMsg) ? new VolatileMessage() : new Message();
            if (msgId == null)
                msgId = "AUTO_ID_" + (count++);
            newMsg.msgId = msgId;
            newMsg.msgTxt = msgTxt;
            newMsg.icon = icon;
            newMsg.duration = duration;
            newMsg.expires = System.currentTimeMillis() + newMsg.duration;

            messages.add(newMsg);
        }
    }

    public static void removeMessage(String msgId) {
        for (int i = 0; i < messages.size(); i++) {
            Message msg = (Message) messages.get(i);

            if (msg.msgId.equals(msgId))
                msg.expires = 0;
        }
    }

    public static void expireMessages() {
        messagesTmp.clear();
        for (int i = 0; i < messages.size(); i++) {
            Message msg = (Message) messages.get(i);

            if (msg.expires > System.currentTimeMillis()) {
                messagesTmp.add(msg);
            }
        }

        ArrayList messagesTmpSwap;
        messagesTmpSwap = messages;
        messages = messagesTmp;
        messagesTmp = messagesTmpSwap;
    }

    public static Message getMessage(int i) {
        return (Message) messages.get(i);
    }

    public static int getMessageCount() {
        return messages.size();
    }

    public static void clearAllMessages() {
        messages.clear();
    }
}

class Message implements Drawable {
    String msgId;
    String msgTxt;
    Bitmap icon;
    long duration;
    long expires;
    String miniTxt;

    boolean fullMessage = true;
    float miniHeight, miniWidth, miniTxtWidth, miniTxtHeight;

    boolean init = false;
    float width, height, iconHeight, txtHeight;
    float pad = 4;

    TextBlock tb;

    public void showFullMessage(boolean fullMessage) {
        this.fullMessage = fullMessage;
    }

    public boolean isImportant() {
        if (icon == ARXMessages.refreshIcon || icon == ARXMessages.refreshCompleteIcon ||
                icon == ARXMessages.downloadErrorIcon || icon == ARXMessages.errorIcon ||
                icon == ARXMessages.warningIcon || icon == ARXMessages.worldIcon) {
            return true;
        } else {
            return false;
        }
    }

    public void init(DrawWindow dw, float maxWidth) {
        dw.setFontSize(12);

        width = maxWidth;
        txtHeight = dw.getTextAscent() + dw.getTextDescent();
        iconHeight = icon.getHeight();
        tb = new TextBlock(msgTxt, 12, maxWidth - icon.getWidth() - pad * 3, Color.rgb(255, 255, 255), Color.rgb(255,
                255, 255), Color.rgb(0, 0, 0), 0, dw);
        height = Math.max(tb.getHeight(), iconHeight) + pad * 2;

        miniHeight = iconHeight + pad * 2;
        miniWidth = icon.getWidth() + pad * 2;

        init = true;
    }

    public void draw(DrawWindow dw) {
        if (fullMessage) {
            long step = (System.currentTimeMillis() % 1000) / 250;

            dw.setFill(true);
            dw.setColor(Color.rgb(255, 255, 255));
            dw.drawRectangle(0, 0, width, height);
            dw.setColor(Color.rgb(0, 0, 0));
            if (icon == ARXMessages.refreshIcon) {
                float spin = 0;
                if (step == 1)
                    spin = 90;
                if (step == 2)
                    spin = 180;
                if (step == 3)
                    spin = 270;
                dw.drawBitmap(icon, pad, height / 2 - icon.getHeight() / 2, spin, 1);
            } else if (icon == ARXMessages.worldIcon) {
                if (step != 0)
                    dw.drawBitmap(icon, pad, miniHeight / 2 - icon.getHeight() / 2, 0, 1);
            } else {
                dw.drawBitmap(icon, pad, height / 2 - icon.getHeight() / 2, 0, 1);
            }
            dw.setFontSize(12);
            dw.drawObject(tb, icon.getWidth() + pad * 2, height / 2 - tb.getHeight() / 2, 0, 1);
        } else {
            long step = (System.currentTimeMillis() % 1000) / 250;

            if (icon == ARXMessages.refreshIcon) {
                float spin = 0;
                if (step == 1)
                    spin = 90;
                if (step == 2)
                    spin = 180;
                if (step == 3)
                    spin = 270;

                dw.drawBitmap(icon, pad, miniHeight / 2 - icon.getHeight() / 2, spin, 1);
            } else if (icon == ARXMessages.worldIcon) {
                if (step != 0)
                    dw.drawBitmap(icon, pad, miniHeight / 2 - icon.getHeight() / 2, 0, 1);
            } else {
                dw.drawBitmap(icon, pad, miniHeight / 2 - icon.getHeight() / 2, 0, 1);
            }
        }
    }

    public float getWidth() {
        if (fullMessage)
            return width;
        else
            return miniWidth;
    }

    public float getHeight() {
        if (fullMessage)
            return height;
        else
            return miniHeight;
    }

    public String toString() {
        return msgId + "<" + (expires - System.currentTimeMillis()) + ">: " + msgTxt;
    }
}

class VolatileMessage extends Message {
    public void init(DrawWindow dw, float maxWidth) {
        dw.setFontSize(12);

        width = maxWidth;
        txtHeight = dw.getTextAscent() + dw.getTextDescent();
        iconHeight = icon.getHeight();
        height = Math.max(txtHeight, iconHeight) + pad * 2;

        miniHeight = iconHeight + pad * 2;
        miniWidth = icon.getWidth() + pad * 2;
        // miniTxtWidth = dw.getTextWidth(miniTxt);
        // miniTxtHeight = txtHeight;

        init = true;
    }

    public void draw(DrawWindow dw) {
        if (fullMessage) {
            dw.setFill(true);
            dw.setColor(Color.rgb(255, 255, 255));
            dw.drawRectangle(0, 0, width, height);
            dw.setColor(Color.rgb(0, 0, 0));
            dw.drawBitmap(icon, pad, height / 2 - icon.getHeight() / 2, 0, 1);
            dw.setFontSize(12);
            dw.drawText(icon.getWidth() + pad * 2, dw.getTextAscent() + pad, msgTxt);
        } else {
            dw.drawBitmap(icon, pad, miniHeight / 2 - icon.getHeight() / 2, 0, 1);
            // dw.drawText(icon.getWidth() + pad*2, dw.getTextAscent()+pad,
            // miniTxt);
        }
    }
}
