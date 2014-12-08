/*
 * UnB - Universidade de Brasília
 * CIC - Departamento de Ciência da Computação
 * IA - Introdução a Inteligência Artificial
 * 
 * @author zidenis
 * @version 0.1 (24/11/2014)
*/

package unb.ia.mapcoloring;

import alice.tuprolog.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Coloração de mapas com restrição de que as áreas adjacentes não podem possuir uma mesma cor.
 * utiliza biblioteca tuProlog que implementa um motor de inferência prolog
 * @author Denis Albuquerque (11/0114388)
 * @version 0.1 (24/11/2014)
 */
public class MapColoring {
    // Base de regras de inferência em Prolog para coloração de mapas
    final String RULES_FILE = "prolog/rules.pl";
    // Base de fatos em Prolog com cores disponíveis para coloração e mapas
    final String KB_FILE = "prolog/facts.pl";
    
    // Objetos tuProlg
    Theory rules;
    Theory kb;
    Prolog engine;

    /**
     * Inicializa motor Prolog (tuProlog) e carrega base de conhecimento.
     */
    public MapColoring() {
        try {
            this.rules = new Theory(new FileInputStream(RULES_FILE));
            this.kb = new Theory(new FileInputStream(KB_FILE));
            this.engine = new Prolog();
            engine.setTheory(rules);
            engine.addTheory(kb);
        } catch (IOException ex) {
            Logger.getLogger(MapColoring.class.getName()).log(Level.SEVERE, "theory file not found", ex);
        } catch (InvalidTheoryException ex) {
            Logger.getLogger(MapColoring.class.getName()).log(Level.SEVERE, "invalid theory file", ex);
        }
    }
    
    /**
     * Obtém a lista de cores disponíveis para coloração do mapa.
     * @return lista de cores
     */
    public List<String> getColors() {
        List<String> listColors = new ArrayList<>();
        try {
            // Cores disponíveis na KB
            // cor(id_Cor)
            //  id_Cor : identificador da cor
            SolveInfo goal = engine.solve("cor(X).");
            listColors.add(goal.getTerm("X").toString());
            while (engine.hasOpenAlternatives()) {
                listColors.add(engine.solveNext().getTerm("X").toString());
            }
        } catch (NoSolutionException ex) {
            return listColors;
        } catch (NoMoreSolutionException | UnknownVarException | MalformedGoalException ex) {
            Logger.getLogger(MapColoring.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listColors;
    }
    
    /**
     * Obtém a lista de nomes dos mapas contidos na Base de Conhecimento
     * @return lista de nomes dos mapas
     */
    public List<String> getMaps() {
        List<String> listMaps = new ArrayList<>();
        try {
            // Mapas da KB
            // mapa(id_Mapa, lista_Areas)
            //  id_Mapa : idenficiador do mapa (ex.: nordeste)
            //  lista_Areas : lista de areas do mapa
            SolveInfo goal = engine.solve("mapa(X, L).");
            listMaps.add(goal.getTerm("X").toString());
            while (engine.hasOpenAlternatives()) {
                listMaps.add(engine.solveNext().getTerm("X").toString());
            }
        } catch (NoSolutionException ex) {
            return listMaps;
        } catch (MalformedGoalException | UnknownVarException | NoMoreSolutionException ex) {
            Logger.getLogger(MapColoring.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listMaps;
    }
    
    /**
     * Obtém uma lista de áreas de um mapa
     * @param map nome do mapa
     * @return lista de nomes das áreas do mapa
     */
    public List<String> getAreasMap(String map) {
        List listAreas = new ArrayList<>();
        try {
            // Mapas da KB
            // mapa(id_Mapa, lista_Areas)
            //  id_Mapa : idenficiador do mapa (ex.: nordeste)
            //  lista_Areas : lista de areas do mapa
            SolveInfo goal = engine.solve("mapa(".concat(map).concat(", Areas)."));
            Struct st = (Struct) goal.getTerm("Areas");
            Iterator iter = st.listIterator();
            while(iter.hasNext()) {
                Struct area = (Struct)iter.next();
                listAreas.add(area.getArg(0).toString());
            }
        } catch (NoSolutionException | UnknownVarException ex) {
            return listAreas;
        } catch (MalformedGoalException ex) {
            Logger.getLogger(MapColoring.class.getName()).log(Level.SEVERE, null, ex);
        }
        listAreas.sort(String.CASE_INSENSITIVE_ORDER);
        return listAreas;
    }
    
    /**
     * Obtém uma lista de áreas adjacentes à uma área de um mapa
     * @param map nome do mapa
     * @param area nome da área
     * @return lista de nomes das áreas adjacentes à área informada
     */
    public List<String> getAreasAdjacentes(String map, String area) {
        List listAreas = new ArrayList<>();
        try {
            // Mapas da KB
            // mapa(id_Mapa, lista_Areas)
            //  id_Mapa : idenficiador do mapa (ex.: nordeste)
            //  lista_Areas : lista de areas do mapa
            SolveInfo goal = engine.solve("mapa(".concat(map).concat(", Areas)."));
            Struct st = (Struct) goal.getTerm("Areas");
            String idArea = null;
            Iterator iter = st.listIterator();
            while(iter.hasNext()) {
                Struct ar = (Struct)iter.next();
                if (area.equalsIgnoreCase(ar.getArg(0).toString())) {
                    idArea = ar.getArg(1).toString();
                }
            }
            iter = st.listIterator();
            // Constrói lista de áreas adjacentes
            while(iter.hasNext()) {
                Struct ar = (Struct)iter.next();
                Struct listAdjacentes = (Struct)ar.getArg(2);
                Iterator iter2 = listAdjacentes.listIterator();
                while(iter2.hasNext()){
                    Var adj = (Var)iter2.next();
                    if (adj.toString().equals(idArea)) {
                        listAreas.add(ar.getArg(0).toString());
                    }
                }
            }
        } catch (NoSolutionException | UnknownVarException ex) {
            return listAreas;
        } catch (MalformedGoalException ex) {
            Logger.getLogger(MapColoring.class.getName()).log(Level.SEVERE, null, ex);
        }
        listAreas.sort(String.CASE_INSENSITIVE_ORDER);
        return listAreas;
    }
    
    /**
     * Obtem uma solução de coloração de mapa
     * @param map nome do mapa a ser colorido
     * @return lista de pares (Nome da Área, Cor da Área)
     */
    public List<Pair> colorMap(String map) {
        return this.colorMap(map, null, null);
    }
    
    /**
     * Obtem uma solução de coloração de mapa
     * considerando uma restrição de que uma área deve possui uma cor determinada.
     * @param map nome do mapa a ser colorido
     * @param area area cuja cor deverá ser restrita
     * @param cor cor definida como restrição da área
     * @return lista de pares (Nome da Área, Cor da Área)
     */
    public List<Pair> colorMap(String map, String area, String cor) {
        List<Pair> listAreas = new ArrayList<>();
        try {
            if (area != null && cor != null) {
                // colorir_Mapa(Id_Mapa, idArea, idCor) :-
                //  Id_Mapa : identificador do mapa na KB (ex: nordeste)
                //  idArea : identificador de área do mapa (ex: pb)
                //  idCor : identificador de cor (ex: red)
                SolveInfo colorirGoal = engine.solve("colorir_Mapa(" + map + "," + area + "," + cor + ").");
            }
            else {
                // colorir_Mapa(Id_Mapa)
                //  Id_Mapa : identificador do mapa na KB (ex: nordeste)
                SolveInfo colorirGoal = engine.solve("colorir_Mapa(" + map + ").");
            }
            // cor_Area é um fato inserido na base dinamicamente
            // na medida em que o mapa foi colorido.
            // cor_Area(id_Area, cor_Area)
            //  id_Area : idenficiador da área (ex.: rn)
            //  cor_Area : identificador da cor associada à área (ex: vermelha)
            SolveInfo corAreaGoal = engine.solve("cor_Area(X,Y).");
            Pair corArea = new Pair(corAreaGoal.getTerm("X").toString(), corAreaGoal.getTerm("Y").toString());
            listAreas.add(corArea);
            while (engine.hasOpenAlternatives()) {
                corAreaGoal = engine.solveNext();
                corArea = new Pair(corAreaGoal.getTerm("X").toString(), corAreaGoal.getTerm("Y").toString());
                listAreas.add(corArea);
            }
        } catch (NoSolutionException | UnknownVarException ex) {
            return listAreas;
        } catch (MalformedGoalException | NoMoreSolutionException ex) {
            Logger.getLogger(MapColoring.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listAreas;
    }
}