/*
 * UnB - Universidade de Brasília
 * CIC - Departamento de Ciência da Computação
 * IA - Introdução a Inteligência Artificial
 * 
 * @author zidenis
 * @version 0.1 (24/11/2014)
*/
package unb.ia.mapcoloring;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;

/**
 * Controler da Interface Gráfica de Usuário.
 * @author Denis Albuquerque (11/0114388)
 * @version 0.1 (24/11/2014)
 */
public class GUIController implements Initializable {
    @FXML
    private ListView<String> mapasListView; // Componente que exibe a Lista de Mapas 
    @FXML
    private ListView<String> areasListView; // Componente que exibe a Lista de Áreas do Mapa selecionado
    @FXML
    private ListView<String> adjacenciasListView; // Componenete que exibe as áreas adjacentes a uma área selecionada
    @FXML
    private SwingNode graphSwingNode; // Componente que exibirá o grafo de ajdacências colorido
    
    private Main main; // Referência para a aplicação

    /**
     * inicializa o Controller da Interface Gráfica
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Ação de clicar em um item da lista de Mapas
        mapasListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                adjacenciasListView.setItems(null);
                List<String> areas = main.getAreas(newValue);
                ObservableList areaList = FXCollections.observableArrayList();
                areaList.addAll(areas);
                areasListView.setItems(areaList);
                List<MenuItem> listCores = new ArrayList<>();
                // Cria Menu de Contexto para Definição de Restrição de Cor de Área
                for(String cor: main.getCores()) {
                    MenuItem menuItem = new MenuItem(cor);
                    menuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            graphSwingNode.setContent(main.buildMapGraph(mapasListView.getSelectionModel().getSelectedItem(),areasListView.getSelectionModel().getSelectedItem(),menuItem.getText()));
                        }
                    });
                    listCores.add(menuItem);
                }
                ContextMenu contextMenu;
                contextMenu = new ContextMenu(listCores.toArray(new MenuItem[listCores.size()]));
                areasListView.setCellFactory(ContextMenuListCell.<String>forListView(contextMenu));
                graphSwingNode.setContent(main.buildMapGraph(mapasListView.getSelectionModel().getSelectedItem()));
            }
        });
        // Ação ao clicar em um item da lista de áreas 
        areasListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    adjacenciasListView.setItems(FXCollections.observableArrayList(main.getAdjacencias(mapasListView.getSelectionModel().getSelectedItem(), newValue)));
                }
            }
        });
    }

    /**
     * Inicializa controler com referência para o Main 
     * popula o componente com a lista de mapas
     * @param main referência para a aplicação
     */
    public void setMain(Main main) {
        this.main = main;
        mapasListView.setItems(FXCollections.observableArrayList(main.getMapas()));
    }
}