import java.util.List;

public class ListMessage implements Message{
    public List<String> listFiles;

    public ListMessage(List<String> listFiles) {
        this.listFiles = listFiles;
    }

    public List<String> getListFiles() {
        return listFiles;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.LIST_MESSAGE;
    }
}
