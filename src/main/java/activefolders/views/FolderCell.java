package activefolders.views;

import activefolders.Folder;
import javafx.scene.control.ListCell;

import java.util.function.Function;

/**
 * Created by daividsilverio on 2014-08-06.
 */
public class FolderCell extends ListCell<Folder> {
    FolderItem folderItem;

    public FolderCell(Function<Folder, Void> folderDetailOpener) {
        folderItem = new FolderItem();
        folderItem.setFolderDetailOpener(folderDetailOpener);
    }

    @Override
    protected void updateItem(Folder item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            folderItem.setFolder(item);
            setGraphic(folderItem);
        }
    }
}
