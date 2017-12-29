package VFCalculator;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;

import java.util.*;

public class MainUIController {
    @FXML
    private ComboBox<Integer> attrNumBox;

    @FXML
    private ComboBox<Integer> appNumBox;

    @FXML
    private ComboBox<Integer> siteNumBox;

    @FXML
    private FlowPane PKFlowPane;

    @FXML
    private TableView<ObservableList<String>> UMTable;

    @FXML
    private TableView<ObservableList<String>> AMTable;

    @FXML
    private TableView<ObservableList<String>> AATable;

    @FXML
    private TableView<ObservableList<String>> CATable;

    @FXML
    private TextArea progTextArea;

    @FXML
    private TextArea VFTextArea;

    private ObservableList<ObservableList<String>> usageMatrix = FXCollections.observableArrayList();
    private ObservableList<ObservableList<String>> accMatrix = FXCollections.observableArrayList();
    private ObservableList<ObservableList<String>> aaMatrix = FXCollections.observableArrayList();
    private ObservableList<ObservableList<String>> caMatrix = FXCollections.observableArrayList();

    private int attrNum;
    private int appNum;
    private int siteNum;

    private final int DISTANCE_BETWEEN_COLUMN_AND_ROW = 1;
    private final int DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY = 1;
    private final int ROW_HEADER = 0;
    private final int TOP_ATTRIBUTE = 1;
    private final int BOT_ATTRIBUTE = 2;
    private final int TQACCESS = 1;
    private final int BQACCESS = 2;
    private final int OQACCESS = 3;

    private Boolean[] PKFlag = new Boolean[10];

    @FXML
    private void initialize() {
        Arrays.fill(PKFlag, false);
        progTextArea.setEditable(false);
    }

    @FXML
    private void handleAttrChangeVal() {
        attrNum = attrNumBox.getValue();
        createInputMatrix(UMTable, usageMatrix, attrNum, "A");
        PKFlowPane.getChildren().clear();

        for (int i = 1; i <= attrNum; i++) {
            ToggleButton btn = new ToggleButton();
            btn.setPrefSize(40,30);
            btn.setId(Integer.toString(i));
            btn.setText("A" + Integer.toString(i));
            btn.setOnAction((ActionEvent e) -> {
                if (btn.isSelected()) {
                    PKFlag[Integer.parseInt(btn.getId()) - 1] = true;
                }
                else
                    PKFlag[Integer.parseInt(btn.getId()) - 1] = false;
            });
            PKFlowPane.getChildren().add(btn);
        }
    }

    @FXML
    private void handleAppChangeVal() {
        appNum = appNumBox.getValue();
        createInputMatrix(UMTable, usageMatrix, attrNum,"A");
        createInputMatrix(AMTable, accMatrix, siteNum,"S");
    }

    @FXML
    private void handleSiteChangeVal() {
        siteNum = siteNumBox.getValue();
        createInputMatrix(AMTable, accMatrix, siteNum,"S");
    }

    private void createInputMatrix(TableView<ObservableList<String>> table,
                                   ObservableList<ObservableList<String>> matrix ,
                                   int colNum,
                                   String headerPrefix) {
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().clear();
        table.getItems().clear();

        //edit table cell on focus
        table.getFocusModel().focusedCellProperty().addListener(
                (ObservableValue<? extends TablePosition> observable, TablePosition oldValue, TablePosition newValue ) ->
                {
                    if ( newValue != null )
                    {
                        Platform.runLater( () ->
                        {
                            table.edit( newValue.getRow(), newValue.getTableColumn() );
                        } );
                    }
                }
        );


        //create list of row with empty data
        for (int i = 0; i < appNum; i++) {
            ObservableList<String> aRow = FXCollections.observableArrayList();
            //add row header
            aRow.add("Q" + Integer.toString(i + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY));
            for (int j = 1; j < colNum + DISTANCE_BETWEEN_COLUMN_AND_ROW; j++) {
                aRow.add("");
            }
            matrix.add(aRow);
        }

        //create column and bind row index to corresponding column
        TableColumn<ObservableList<String>, String> col0 = new TableColumn<>();
        col0.setCellValueFactory(col -> new SimpleStringProperty(col.getValue().get(ROW_HEADER)));
        col0.setPrefWidth(60);
        table.getColumns().add(col0);
        for (int i = 1; i < colNum + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY; i++) {
            String colHeaderName = headerPrefix + Integer.toString(i);
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(colHeaderName);
            col.setPrefWidth(60);
            final int idx = i;
            col.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(idx)));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setOnEditCommit(
                    (TableColumn.CellEditEvent<ObservableList<String>, String> t) ->
                        (t.getTableView().getItems().get(t.getTablePosition().getRow()))
                            .set(idx, t.getNewValue())
            );
            table.getColumns().add(col);

        }
        table.setItems(matrix);
    }

    @FXML
    private void handleCalculate() {
        AATable.getColumns().clear();
        aaMatrix.clear();
        CATable.getColumns().clear();
        caMatrix.clear();
        progTextArea.clear();
        VFTextArea.clear();

        List<Integer> PKs = determinePKs();
        if (PKs == null)
            return;

        calculateAAMatrix();
        List<Integer> attrOrders = calculateCAMatrix();
        calculateVF(attrOrders, PKs);
    }


    private void calculateVF(List<Integer> attrOrders, List<Integer> PKs) {
        progTextArea.appendText("\n********Calculate Vertical Fragment********\n");
        int XPos = determineX(attrOrders);
        showFragment(createAFragment(XPos, PKs, attrOrders, TOP_ATTRIBUTE));
        showFragment(createAFragment(XPos, PKs, attrOrders, BOT_ATTRIBUTE));
    }

    private void showFragment(Set<Integer> aFragment) {
        VFTextArea.appendText("[");
        List<Integer> aFragmentList = new ArrayList<>(aFragment);
        for (int i = 0; i < aFragmentList.size() - 1; i++) {
            VFTextArea.appendText("A" + (aFragmentList.get(i) + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + ",");
        }
        VFTextArea.appendText("A" + (aFragmentList.get(aFragment.size() - 1)
                + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + "]\n");
    }

    private Set<Integer> createAFragment(int xPos, List<Integer> pKs, List<Integer> attrOrders, int mode) {
        Set<Integer> aFragment = new HashSet<>();
        aFragment.addAll(pKs);
        if (mode == TOP_ATTRIBUTE) {
            aFragment.addAll(attrOrders.subList(0, xPos));
        } else if (mode == BOT_ATTRIBUTE) {
            aFragment.addAll(attrOrders.subList(xPos, attrOrders.size()));
        }
        return aFragment;
    }

    private List<Integer> determinePKs() {
        List<Integer> PKs = new ArrayList<>();
        for (int currFlag = 0; currFlag < PKFlag.length; currFlag++) {
            if (PKFlag[currFlag])
                PKs.add(currFlag);
        }
//        if (PKs.size() == 0) {
//            showMessageError("You must add Primary key");
//            return null;
//        }
        return PKs;
    }

    private void showMessageError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.initOwner(this);
        alert.setTitle("Error");
        alert.setHeaderText("Please correct error!");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private int determineX(List<Integer> attrOrders) {
        int maxZ = calculateZ(attrOrders, 1);
        int maxZPos = 2;

        progTextArea.appendText("Z = " + maxZ + "\n");

        //top attribute must not be empty
        for (int partition2Idx = 2; partition2Idx < attrOrders.size(); partition2Idx++) {
            int Z = calculateZ(attrOrders, partition2Idx);

            progTextArea.appendText("Z = " + Z + "\n");

            if (Z > maxZ) {
                maxZ = Z;
                maxZPos = partition2Idx;
            }
        }

        showXPartition(attrOrders, maxZPos);

        return maxZPos;
    }

    private void showXPartition(List<Integer> attrOrders, int maxZPos) {
        progTextArea.appendText("Best Partition: {");
        for (int idxAttr = 0; idxAttr < maxZPos - 1; idxAttr++) {
            progTextArea.appendText("A" + (attrOrders.get(idxAttr)
                    + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + ",");
        }
        progTextArea.appendText("A" + (attrOrders.get(maxZPos - 1)
                + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + "} {");

        for (int idxAttr = maxZPos; idxAttr < attrOrders.size() - 1; idxAttr++)  {
            progTextArea.appendText("A" + (attrOrders.get(idxAttr)
                    + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + ",");
        }
        progTextArea.appendText("A" + (attrOrders.get(attrOrders.size() - 1)
                + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + "}\n");
    }

    private void showCurrentPartition(List<Integer> topAttr, List<Integer> botAttr) {
        progTextArea.appendText("Partition: {");
        for (int i = 0; i < topAttr.size() - 1; i++) {
            progTextArea.appendText("A" + (topAttr.get(i) + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + ",");
        }
        progTextArea.appendText("A" + (topAttr.get(topAttr.size() - 1)
                + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + "} {");

        for (int i = 0; i < botAttr.size() - 1; i++)  {
            progTextArea.appendText("A" + (botAttr.get(i) + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + ",");
        }
        progTextArea.appendText("A" + (botAttr.get(botAttr.size() - 1) +
                DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + "}:\n");
    }

    private int calculateZ(List<Integer> attrOrders, int splitter) {
        List<Integer> topAttr = new ArrayList<>(attrOrders.subList(0, splitter));
        List<Integer> botAttr = new ArrayList<>(attrOrders.subList(splitter, attrOrders.size()));

        showCurrentPartition(topAttr, botAttr);

        List<Integer> TQAccess = findTOrBQQueryAccess(topAttr);
        List<Integer> BQAccess = findTOrBQQueryAccess(botAttr);
        List<Integer> OQAccess = findBothQueryAccess(TQAccess, BQAccess);

        int CTQ = getTotalTimeQueryAccessPartitionAttr(TQAccess);
        int CBQ = getTotalTimeQueryAccessPartitionAttr(BQAccess);
        int COQ = getTotalTimeQueryAccessPartitionAttr(OQAccess);

        showCalculateZProgress(TQACCESS, TQAccess, CTQ);
        showCalculateZProgress(BQACCESS, BQAccess, CBQ);
        showCalculateZProgress(OQACCESS, OQAccess, COQ);

        return CTQ * CBQ - COQ * COQ;
    }

    private void showCalculateZProgress(int mode, List<Integer> queriesAccess, int totalQueriesVal) {
        String msgQueriesAccess = "";
        String msgQueriesVal = "";

        switch (mode) {
            case TQACCESS:
                msgQueriesAccess += "TQ = ";
                msgQueriesVal += "CTQ = ";
                break;
            case BQACCESS:
                msgQueriesAccess += "BQ = ";
                msgQueriesVal += "CBQ = ";
                break;
            case OQACCESS:
                msgQueriesAccess += "OQ = ";
                msgQueriesVal += "COQ = ";
                break;
        }

        progTextArea.appendText(msgQueriesAccess);
        if (queriesAccess.size() != 0)
            showQueryAccess(queriesAccess);
        else
            progTextArea.appendText("None\n");
        progTextArea.appendText(msgQueriesVal + Integer.toString(totalQueriesVal) + "\n");

    }

    private void showQueryAccess(List<Integer> queriesAccess) {
        for (int queries : queriesAccess) {
            progTextArea.appendText(" Q" + Integer.toString(queries + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + ",");
        }
        progTextArea.appendText("\n");
    }

    private int getTotalTimeQueryAccessPartitionAttr(List<Integer> queries) {
        int sum = 0;
        for (int query : queries) {
            ObservableList<String> accessSite = accMatrix.get(query);
            for (int idxSite = 1; idxSite < accessSite.size(); idxSite++) {
                sum += Integer.parseInt(accessSite.get(idxSite));
            }
        }
        return sum;
    }

    private List<Integer> findBothQueryAccess(List<Integer> TQAccess, List<Integer> BQAccess) {
        List<Integer> TBQueryAccess = new ArrayList<>();
        TBQueryAccess.addAll(TQAccess);
        TBQueryAccess.addAll(BQAccess);

        List<Integer> OQAccess = new ArrayList<>();
        for (int row = 0; row < usageMatrix.size(); row++) {
            if (!TBQueryAccess.contains(row))
                OQAccess.add(row);
        }
        return OQAccess;
    }

    private List<Integer> findTOrBQQueryAccess(List<Integer> attrs) {
        List<Integer> queriesAccess = new ArrayList<>();
        for (int row = 0; row < usageMatrix.size(); row++) {
            List<String> aRow = usageMatrix.get(row);
            List<Integer> tmp = new ArrayList<>();
            for (int col = 1; col < aRow.size(); col++) {
                if (aRow.get(col).equals("1"))
                    tmp.add(col - DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY);
            }
            if (tmp.size() == attrs.size() & tmp.containsAll(attrs))
                queriesAccess.add(row);
        }
        return queriesAccess;
    }

    private List<Integer> calculateCAMatrix() {
        progTextArea.appendText("\n********Calculate CA Matrix********\n");
        List<Integer> attrOrders = determineAttrOrder();
        createCAMatrixData(attrOrders);
        showCATableView(attrOrders);

        return attrOrders;
    }

    private void showCATableView(List<Integer> attrOrders) {
        TableColumn<ObservableList<String>, String> col0 = new TableColumn<>();
        col0.setCellValueFactory(col -> new SimpleStringProperty(col.getValue().get(0)));
        col0.setPrefWidth(60);
        CATable.getColumns().add(col0);
        for (int i = 1; i <= attrNum; i++) {
            final int idx = i;
            String colHeaderName = "A" + Integer.toString(attrOrders
                    .get(i - DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + DISTANCE_BETWEEN_COLUMN_AND_ROW);
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(colHeaderName);
            col.setPrefWidth(60);
            col.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(idx)));
            CATable.getColumns().add(col);

        }

        CATable.setItems(caMatrix);
    }

    private void createCAMatrixData(List<Integer> attrOrders) {
        caMatrix.clear();
        for (int i = 0; i < attrNum; i++) {
            int posRow = attrOrders.get(i);
            ObservableList<String> aRow = FXCollections.observableArrayList();
            aRow.add("A" + Integer.toString(posRow + 1));

            for (int j = 0; j < attrNum; j++) {
                int posCol = attrOrders.get(j);
                aRow.add(aaMatrix.get(posRow).get(posCol + 1));
            }
            caMatrix.add(aRow);
        }
    }

    private List<Integer> determineAttrOrder() {
        List<Integer> attrOrders = new ArrayList<>();
        progTextArea.appendText("Determine: A1, A2\n");
        attrOrders.add(0);
        attrOrders.add(1);
        for (int i = 2; i < aaMatrix.size(); i++) {
            progTextArea.appendText("Determine position for A" + Integer.toString(i + 1) + "\n");
            int bestPos = determineBestPos2Add(attrOrders, i);
            attrOrders.add(bestPos, i);
            showCADetermineProgress(attrOrders);
        }
        return attrOrders;
    }

    private int determineBestPos2Add(List<Integer> attrOrders, int colInsert) {
        int maxContVal = 0;
        int maxContPos = 0;
        for (int currAttr = 0; currAttr <= attrOrders.size(); currAttr++) {

            showCAContProgress(attrOrders, colInsert, currAttr);

            int contVal = calculateCont(attrOrders, colInsert, currAttr);

            if (contVal > maxContVal) {
                maxContVal = contVal;
                maxContPos = currAttr;
            }
        }
        return maxContPos;
    }

    private void showCADetermineProgress(List<Integer> attrOrders) {
        progTextArea.appendText("Determine: ");
        for (int i = 0; i < attrOrders.size() - 1; i++) {
            progTextArea.appendText("A" + Integer.toString(attrOrders.get(i) + 1) + " - ");
        }
        progTextArea.appendText("A" + Integer.toString(attrOrders.get(attrOrders.size() - 1) + 1) + "\n");
    }

    private void showCAContProgress(List<Integer> attrOrders, int colInsert, int currAttr) {
        progTextArea.appendText("+ cont(");
        if (currAttr == 0)
            progTextArea.appendText("_");
        else
            progTextArea.appendText("A" + Integer.toString(attrOrders.get(currAttr - 1)
                    + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY));

        progTextArea.appendText(" - A" + Integer.toString(colInsert + 1) + " - ");

        if (currAttr + 1 > attrOrders.size())
            progTextArea.appendText("_) = ");
        else
            progTextArea.appendText("A" + Integer.toString(attrOrders.get(currAttr)
                    + DISTANCE_BETWEEN_INTERNAL_INDEX_AND_DISPLAY) + ") = ") ;
    }

    private int calculateCont(List<Integer> attrOrders, int colInsert, int beforePos) {
        if (beforePos == 0) {
            int bondAfter = calculateBond(colInsert, attrOrders.get(beforePos));

            progTextArea.appendText("2 * " + Integer.toString(bondAfter) + " = " + Integer.toString(2 * bondAfter) + "\n");

            return 2 * bondAfter;
        }

        if (beforePos == attrOrders.size()) {
            int bondBefore = calculateBond(attrOrders.get(beforePos - 1), colInsert);

            progTextArea.appendText("2 * " + Integer.toString(bondBefore) + " = " + Integer.toString(2 * bondBefore) + "\n");

            return 2 * bondBefore;
        }

        int bondBefore = calculateBond(attrOrders.get(beforePos - 1), colInsert);
        int bondAfter = calculateBond(colInsert, attrOrders.get(beforePos));
        int bond2Side = calculateBond(attrOrders.get(beforePos - 1), attrOrders.get(beforePos));

        progTextArea.appendText("2 * " + Integer.toString(bondBefore) + " + 2 * " + Integer.toString(bondAfter)
                + " - 2 * " + Integer.toString(bond2Side) + " = "
                + Integer.toString(2 * bondBefore + 2 * bondAfter - 2 * bond2Side) + "\n    ");

        return 2 * bondBefore + 2 * bondAfter - 2 * bond2Side;
    }

    private int calculateBond(int firstPos, int lastPos) {
        int sum = 0;
        for (int i = 1; i <= aaMatrix.size(); i++) {
            //all values corresponding to col parameter must larger than 0
            //0 is row header
            sum += getAffinityValAtCell(firstPos, i) * getAffinityValAtCell(lastPos, i);
        }
        return sum;
    }

    private int getAffinityValAtCell(int row, int col) {
        return Integer.parseInt(aaMatrix.get(row).get(col));
    }


    private void calculateAAMatrix() {
        progTextArea.appendText("********Calculate AA Matrix********\n");
        createAAMatrixData();
        showAATable();

    }

    private void createAAMatrixData() {
        aaMatrix.clear();
        for (int i = 0; i < attrNum; i++) {
            ObservableList<String> aRow = FXCollections.observableArrayList();
            for (int j = 0; j <= attrNum; j++)
                aRow.add("");
            aaMatrix.add(aRow);
        }

        for (int row = 0; row < attrNum; row++) {
            ObservableList<String> aRow = aaMatrix.get(row);
            int rowInCol = row + DISTANCE_BETWEEN_COLUMN_AND_ROW;
            aRow.set(0,"A" + Integer.toString(rowInCol));
            for (int col = rowInCol; col <= attrNum; col++) {
                List<Integer> affinityQueries = getAffinityQueries(rowInCol, col);
                progTextArea.appendText("AA[" + rowInCol + "][" + col + "] = AA[" + col + "][" + rowInCol + "] = ");
                int sum = getTotalTimesQueryAccessAffinityAttr(affinityQueries);
                aRow.set(col, Integer.toString(sum));
                aaMatrix.get(col - DISTANCE_BETWEEN_COLUMN_AND_ROW).set(rowInCol, Integer.toString(sum));
            }
        }
    }

    private void showAATable() {

        TableColumn<ObservableList<String>, String> col0 = new TableColumn<>();
        col0.setCellValueFactory(col -> new SimpleStringProperty(col.getValue().get(0)));
        col0.setPrefWidth(60);
        AATable.getColumns().add(col0);
        for (int i = 1; i <= attrNum; i++) {
            String colHeaderName = "A" + Integer.toString(i);
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(colHeaderName);
            col.setPrefWidth(60);
            col.setStyle("-fx-alignment: CENTER;");
            final int idx = i;
            col.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(idx)));
            AATable.getColumns().add(col);

        }

        AATable.setItems(aaMatrix);
    }

    private int getTotalTimesQueryAccessAffinityAttr(List<Integer> affinityQueries) {
        int sum = 0;
        for (int i = 0; i < affinityQueries.size(); i++) {
            ObservableList<String> accessSite = accMatrix.get(affinityQueries.get(i));
            for (int j = 1; j < accessSite.size() - 1; j++) {
                progTextArea.appendText(accessSite.get(j) + " + ");
                sum += Integer.parseInt(accessSite.get(j));
            }
            progTextArea.appendText(accessSite.get(accessSite.size() - 1));
            sum += Integer.parseInt(accessSite.get(accessSite.size() - 1));
            if (i < affinityQueries.size() - 1) {
                progTextArea.appendText(" + ");
            }
        }
        if (affinityQueries.size() > 0)
            progTextArea.appendText(" = ");
        progTextArea.appendText( sum + "\n");
        return sum;
    }

    private List<Integer> getAffinityQueries(int row, int col) {
        List<Integer> affinityQueries = new ArrayList<>();
        for (int UMRow = 0; UMRow < usageMatrix.size(); UMRow++) {
            if (Integer.parseInt(usageMatrix.get(UMRow).get(row)) == 1 &&
                    Integer.parseInt(usageMatrix.get(UMRow).get(col)) == 1)
                affinityQueries.add(UMRow);
        }
        return affinityQueries;
    }

    @FXML
    private void handleClose() {
        System.exit(0);
    }
}
