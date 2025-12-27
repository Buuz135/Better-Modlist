package com.buuz135.bettermodlist.util;

import com.hypixel.hytale.server.core.Message;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MessageHelper {

    public static ML multiLine(){
        return new ML();
    }

    public static class ML {

        private List<Message> messages;

        public ML() {
            this.messages = new ArrayList<>();
        }

        public ML append(Message message) {
            this.messages.add(message);
            return this;
        }

        public ML nl() {
            this.messages.add(Message.raw("\n"));
            return this;
        }

        public ML separator() {
            this.messages.add(Message.raw("--------------------------").color(Color.DARK_GRAY));
            this.nl();
            return this;
        }

        public Message build() {
            return Message.join(this.messages.toArray(new Message[0]));
        }
    }
}
