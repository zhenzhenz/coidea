package entity;

import config.FileConfig;
import entity.inter.IDocChangeEvent;
import entity.inter.IPortalDocument;
import util.MyLogger;
import processor.EditingOperation;

public class EclipseDocChangeEventFactory {
    public static IDocChangeEvent buildPortalDocChangeEvent(String originDoc, String originPath,
                                                            EditingOperation originOperation) {
        return new IDocChangeEvent() {
            private EditingOperation eclipseEvent = originOperation;
            private String eclipseDoc = originDoc;
            private String path = originPath;
            private MyLogger log = MyLogger.getLogger(IDocChangeEvent.class);

            @Override
            public IPortalDocument getDocument() {
                return new IPortalDocument() {
                    @Override
                    public String getPath() {
                        return path;
                    }

                    @Override
                    public String getGrammar() {
                        return FileConfig.DEFAULT_GRAMMAR;
                    }

                    @Override
                    public String getText() {
                        return eclipseDoc;
                    }
                };
            }

            @Override
            public int getOffset() {
                return eclipseEvent.position;
            }

            @Override
            public int getOldLength() {
                if (eclipseEvent.type == EditingOperation.DELETE) {
                    return eclipseEvent.length;
                } else {
                    return 0;
                }
            }

            @Override
            public int getNewLength() {
                if (eclipseEvent.type == EditingOperation.INSERT) {
                    return eclipseEvent.length;
                } else {
                    return 0;
                }
            }

            @Override
            public CharSequence getOldFragment() {
                if (eclipseEvent.type == EditingOperation.DELETE) {
                    return eclipseEvent.content;
                } else {
                    return null;
                }
            }

            @Override
            public CharSequence getNewFragment() {
                if (eclipseEvent.type == EditingOperation.INSERT) {
                    return eclipseEvent.content;
                } else {
                    return null;
                }
            }

            @Override
            public long getOldTimeStamp() {
                log.error("getOldTimeStamp unsupported");
                return 0;
            }

            @Override
            public String toString() {
                return "IDocChangeEvent {" +
                        "path='" + path + '\'' +
                        "offset = " + getOffset() + "," +
                        "oldLength = " + getOldLength() + "," +
                        "newLength = " + getNewLength() + "," +
                        "oldFragment = " + getOldFragment() + "," +
                        "newFragment = " + getNewFragment() + "," +
                        '}';
            }
        };
    }

    public static EditingOperation buildEditingOperation(int type, int position, int length, String content) {
        EditingOperation result = new EditingOperation();
        result.type = type;
        result.position = position;
        result.length = length;
        result.content = content;
        return result;
    }
}

