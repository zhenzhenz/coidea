package processor;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class EditingOperation implements java.io.Serializable
{
    public static int INSERT = 1;
    public static int DELETE = 2;
    public static int INVALID = -1;

    public int type;
    public int sid;

    public int position;
    public int length;
    public String content;

    public ArrayList<Integer> vec;

    public int iDALTag;


    public EditingOperation()
    {
        type = INVALID;
        vec = new ArrayList<Integer>();
    }

    public static EditingOperation CreateInsertOperation(int position, String content)
    {
        EditingOperation eo = new EditingOperation();

        eo.type = INSERT;
        eo.position = position;
        eo.length = content.length();
        eo.content = content;


        return eo;
    }

    public static EditingOperation CreateDeleteOperation(int position, String content)
    {
        EditingOperation eo = new EditingOperation();

        eo.type = DELETE;
        eo.position = position;
        eo.length = content.length();
        eo.content = content;

        return eo;
    }

    @Override
    public String toString() {
        return "EditingOperation{" +
                "type=" + type +
                ", sid=" + sid +
                ", position=" + position +
                ", length=" + length +
                ", content='" + content + '\'' +
                ", vec=" + vec +
                ", iDALTag=" + iDALTag +
                '}';
    }
}
