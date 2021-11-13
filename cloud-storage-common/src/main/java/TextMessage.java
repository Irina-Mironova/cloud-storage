public class TextMessage implements Message{
    public String msg;

    public TextMessage(String msg) {
        this.msg = msg;
    }

    public String getTextMessage() {
        return msg;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.TEXT_MESSAGE;
    }
}
