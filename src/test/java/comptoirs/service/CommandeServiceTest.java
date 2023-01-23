package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Commande;
import comptoirs.entity.Ligne;
import comptoirs.entity.Produit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class CommandeServiceTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final String VILLE_PETIT_CLIENT = "Berlin";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");

    static final int NUMERO_COMMANDE_PAS_LIVREE  = 99999;
    @Autowired
    private CommandeService service;
    @Autowired
    private ProduitRepository produitDAO;
    @Autowired
    CommandeRepository commandeDao;
    @Test
    void testCreerCommandePourGrosClient() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        assertNotNull(commande.getNumero(), "On doit avoir la clé de la commande");
        assertEquals(REMISE_POUR_GROS_CLIENT, commande.getRemise(),
            "Une remise de 15% doit être appliquée pour les gros clients");
    }

    @Test
    void testCreerCommandePourPetitClient() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertNotNull(commande.getNumero());
        assertEquals(BigDecimal.ZERO, commande.getRemise(),
            "Aucune remise ne doit être appliquée pour les petits clients");
    }

    @Test
    void testCreerCommandeInitialiseAdresseLivraison() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertEquals(VILLE_PETIT_CLIENT, commande.getAdresseLivraison().getVille(),
            "On doit recopier l'adresse du client dans l'adresse de livraison");
    }
    @Test
    void testEnregistrerExpedition() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        commande = service.enregistreExpédition(commande.getNumero());
        assertEquals(LocalDate.now(), commande.getEnvoyeele());
    }
    @Test
    void testMiseAJourDesStocks() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        var lignes = commande.getLignes();
        var unitesEnStockAvant = new ArrayList<Integer>();

        for(int i = 0; i < lignes.size(); i++){
            unitesEnStockAvant.add(lignes.get(i).getProduit().getUnitesEnStock());
        }
        commande = service.enregistreExpédition(commande.getNumero());
        lignes = commande.getLignes();
        var unitesEnStockApres = new ArrayList<Integer>();
        var quantites = new ArrayList<Integer>();

        for(int i = 0; i < lignes.size(); i++){
            unitesEnStockApres.add(lignes.get(i).getProduit().getUnitesEnStock());
            quantites.add(lignes.get(i).getQuantite());
        }

        if(unitesEnStockApres.size() == unitesEnStockAvant.size()){
            for(int i = 0; i < unitesEnStockAvant.size(); i++){
                assertEquals(unitesEnStockAvant.get(i) - quantites.get(i), unitesEnStockApres.get(i));
            }
        }
    }
    @Test
    void testDecrementerStock(){
        var produit = produitDAO.findById(98).orElseThrow();
        int stockAvant = produit.getUnitesEnStock();
        service.enregistreExpédition(99998);
        assertEquals(stockAvant - 10, produit.getUnitesEnStock(), "On doit décrémenter le stock de 20 unités");
    }

    @Test
    public void testEnregistreExpéditionInconnue() {
        Integer commandeNum = 99;
        assertEquals(Optional.empty(),commandeDao.findById(commandeNum));
    }

    @Test
    public void testEnregistreExpéditionPasCommandé() {
        Integer commandeNum = NUMERO_COMMANDE_PAS_LIVREE;
        Commande result = service.enregistreExpédition(commandeNum);
        assertNotNull(result.getEnvoyeele());
        assertEquals(LocalDate.now(), result.getEnvoyeele());
    }
}
