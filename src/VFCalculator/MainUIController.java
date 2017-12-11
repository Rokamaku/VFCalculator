package VFCalculator;

import javafx.beans.property.SimpleStringProperty;
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


        for (int i = 0; i < appNum; i++) {
            ObservableList<String> aRow = FXCollections.observableArrayList();
            aRow.add("Q" + Integer.toString(i + 1));
            for (int j = 1; j < colNum + 1; j++) {
                aRow.add("");
            }
            matrix.add(aRow);
        }


        TableColumn<ObservableList<String>, String> col0 = new TableColumn<>();
        col0.setCellValueFactory(col -> new SimpleStringProperty(col.getValue().get(0)));
        col0.setPrefWidth(60);
        table.getColumns().add(col0);
        for (int i = 1; i < colNum + 1; i++) {
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

        calculateAAMatrix();
        List<Integer> attrOrders = calculateCAMatrix();
        calculateVF(attrOrders);
    }

    private void calculateVF(List<Integer> attrOrders) {
        progTextArea.appendText("\n********Calculate Vertical Fragment********\n");
        int XPos = determineX(attrOrders);
        List<Integer> PKs = determinePKs(XPos);
        showFragment(createAFragment(XPos, PKs, attrOrders, 1));
        showFragment(createAFragment(XPos, PKs, attrOrders, 2));
    }

    private void showFragment(Set<Integer> aFragment) {
        VFTextArea.appendText("[");
        List<Integer> aFragmentList = new ArrayList<>(aFragment);
        for (int i = 0; i < aFragmentList.size() - 1; i++) {
            VFTextArea.appendText("A" + (aFragmentList.get(i) + 1) + ",");
        }
        VFTextArea.appendText("A" + (aFragmentList.get(aFragment.size() - 1) + 1) + "]\n");
    }

    private Set<Integer> createAFragment(int xPos, List<Integer> pKs, List<Integer> attrOrders, int mode) {
        Set<Integer> aFragment = new HashSet<>();
        aFragment.addAll(pKs);
        if (mode == 1) {
            aFragment.addAll(attrOrders.subList(0, xPos));
        } else if (mode == 2) {
            aFragment.addAll(attrOrders.subList(xPos, attrOrders.size()));
        }
        return aFragment;
    }

    private List<Integer> determinePKs(int XPos) {
        List<Integer> PKs = new ArrayList<>();
        for (int i = 0; i < PKFlag.length; i++) {
            if (PKFlag[i])
                PKs.add(i);
        }
        if (PKs.size() == 0)
            showMessageError("You must add Primary key");
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
        int maxZPos = 0;

        progTextArea.appendText("Z = " + maxZ + "\n");

        for (int splitter = 2; splitter < attrOrders.size(); splitter++) {
            int Z = calculateZ(attrOrders, splitter);

            progTextArea.appendText("Z = " + Z + "\n");

            if (Z > maxZ) {
                maxZ = Z;
                maxZPos = splitter;
            }
        }

        showXPartition(attrOrders, maxZPos);

        return maxZPos;
    }

    private void showXPartition(List<Integer> attrOrders, int maxZPos) {
        progTextArea.appendText("Best Partition: {");
        for (int i = 0; i < maxZPos - 1; i++) {
            progTextArea.appendText("A" + (attrOrders.get(i) + 1) + ",");
        }
        progTextArea.appendText("A" + (attrOrders.get(maxZPos - 1) + 1) + "} {");

        for (int i = maxZPos; i < attrOrders.size() - 1; i++)  {
            progTextArea.appendText("A" + (attrOrders.get(i) + 1) + ",");
        }
        progTextArea.appendText("A" + (attrOrders.get(attrOrders.size() - 1) + 1) + "}\n");
    }

    private void showCurrentPartition(List<Integer> topAttr, List<Integer> botAttr) {
        progTextArea.appendText("Partition: {");
        for (int i = 0; i < topAttr.size() - 1; i++) {
            progTextArea.appendText("A" + (topAttr.get(i) + 1) + ",");
        }
        progTextArea.appendText("A" + (topAttr.get(topAttr.size() - 1) + 1) + "} {");

        for (int i = 0; i < botAttr.size() - 1; i++)  {
            progTextArea.appendText("A" + (botAttr.get(i) + 1) + ",");
        }
        progTextArea.appendText("A" + (botAttr.get(botAttr.size() - 1) + 1) + "}:\n");
    }

    private int calculateZ(List<Integer> attrOrders, int splitter) {
        List<Integer> topAttr = new ArrayList<>(attrOrders.subList(0, splitter));
        List<Integer> botAttr = new ArrayList<>(attrOrders.subList(splitter, attrOrders.size()));

        showCurrentPartition(topAttr, botAttr);

        List<Integer> TQAccess = findTOrBQueryAccess(topAttr);
        List<Integer> BQAccess = findTOrBQueryAccess(botAttr);
        List<Integer> OQAccess = findBothQueryAccess(TQAccess, BQAccess, attrOrders);

        int CTQ = getTotalTimeQueryAccessPartitionAttr(TQAccess);
        int CBQ = getTotalTimeQueryAccessPartitionAttr(BQAccess);
        int COQ = getTotalTimeQueryAccessPartitionAttr(OQAccess);
        progTextArea.appendText("CTO =" + CTQ + "\n");
        progTextArea.appendText("CBQ =" + CBQ + "\n");
        progTextArea.appendText("COQ =" + COQ + "\n");
        return CTQ * CBQ - COQ * COQ;
    }

    private int getTotalTimeQueryAccessPartitionAttr(List<Integer> queries) {
        int sum = 0;
        for (int i = 0; i < queries.size(); i++) {
            ObservableList<String> accessSite = accMatrix.get(queries.get(i));
            for (int j = 1; j < accessSite.size(); j++) {
                sum += Integer.parseInt(accessSite.get(j));
            }
        }
        return sum;
    }

    private List<Integer> findBothQueryAccess(List<Integer> TQAccess, List<Integer> BQAccess, List<Integer> attrOrders) {
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

    private List<Integer> findTOrBQueryAccess(List<Integer> attrs) {
        List<Integer> queriesAccess = new ArrayList<>();
        for (int row = 0; row < usageMatrix.size(); row++) {
            List<String> aRow = usageMatrix.get(row);
            List<Integer> tmp = new ArrayList<>();
            for (int col = 1; col < aRow.size(); col++) {
                if (aRow.get(col).equals("1"))
                    tmp.add(col - 1);
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
            String colHeaderName = "A" + Integer.toString(attrOrders.get(i - 1) + 1);
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
        for (int i = 0; i <= attrOrders.size(); i++) {
            int contVal = calculateCont(attrOrders, colInsert, i);

            showCAContProgress(attrOrders, colInsert, i, contVal);

            if (contVal > maxContVal) {
                maxContVal = contVal;
                maxContPos = i;
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

    private void showCAContProgress(List<Integer> attrOrders, int colInsert, int i, int contVal) {
        progTextArea.appendText("+ cont(");
        if (i == 0)
            progTextArea.appendText("_");
        else
            progTextArea.appendText("A" + Integer.toString(attrOrders.get(i - 1)));

        progTextArea.appendText(" - A" + Integer.toString(colInsert + 1) + " - ");

        if (i + 1 > attrOrders.size())
            progTextArea.appendText("_) = " + Integer.toString(contVal) + "\n");
        else
            progTextArea.appendText("A" + Integer.toString(attrOrders.get(i)) + ") = " + Integer.toString(contVal) + "\n") ;
    }

    private int calculateCont(List<Integer> attrOrders, int colInsert, int beforePos) {
        if (beforePos == 0)
            return 2 * calculateBond(colInsert, attrOrders.get(beforePos));
        if (beforePos == attrOrders.size())
            return 2 * calculateBond(attrOrders.get(beforePos - 1), colInsert);
        return 2 * calculateBond(attrOrders.get(beforePos - 1), colInsert)
                + 2 * calculateBond(colInsert, attrOrders.get(beforePos))
                - 2 * calculateBond(attrOrders.get(beforePos - 1), attrOrders.get(beforePos));
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
            int rowInCol = row + 1;
            aRow.set(0,"A" + Integer.toString(rowInCol));
            for (int col = rowInCol; col <= attrNum; col++) {
                List<Integer> affinityQueries = getAffinityQueries(rowInCol, col);
                progTextArea.appendText("AA[" + rowInCol + "][" + col + "] = AA[" + col + "][" + rowInCol + "] = ");
                int sum = getTotalTimesQueryAccessAffinityAttr(affinityQueries);
                aRow.set(col, Integer.toString(sum));
                aaMatrix.get(col - 1).set(rowInCol, Integer.toString(sum));
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
