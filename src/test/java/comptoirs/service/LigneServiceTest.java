package comptoirs.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.ConstraintViolationException;
import java.util.Optional;
import java.util.NoSuchElementException;

import comptoirs.entity.Commande;
import comptoirs.entity.Ligne;
import comptoirs.entity.Produit;
import comptoirs.dao.ProduitRepository;
import comptoirs.dao.CommandeRepository;


@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class LigneServiceTest {
    static final int NUMERO_COMMANDE_DEJA_LIVREE = 99999;
    static final int NUMERO_COMMANDE_PAS_LIVREE  = 99998;
    static final int REFERENCE_PRODUIT_DISPONIBLE_1 = 93;
    static final int REFERENCE_PRODUIT_DISPONIBLE_2 = 94;
    static final int REFERENCE_PRODUIT_DISPONIBLE_3 = 95;
    static final int REFERENCE_PRODUIT_DISPONIBLE_4 = 96;
    static final int REFERENCE_PRODUIT_INDISPONIBLE = 97;
    static final int UNITES_COMMANDEES_AVANT = 0;
    static final int REFERENCE_PRODUIT_INEXISTANTE = 100;
    static final int NUMERO_COMMANDE_INEXISTANTE = 91;

    @Autowired
    LigneService service;

    @Autowired
    ProduitRepository produitDao;

    @Autowired
    CommandeRepository commandeDao;

    @Test
    void onPeutAjouterDesLignesSiPasLivre() {
        var ligne = service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 1);
        assertNotNull(ligne.getId(),
        "La ligne doit être enregistrée, sa clé générée"); 
    }

    @Test
    void laQuantiteEstPositive() {
        assertThrows(ConstraintViolationException.class, 
            () -> service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 0),
            "La quantite d'une ligne doit être positive");
    }

    @Test
    void testAjoutLigneDejaEnvoyée() {
        Integer commandeNum = NUMERO_COMMANDE_DEJA_LIVREE;
        Integer produitRef = REFERENCE_PRODUIT_DISPONIBLE_1;
        int quantite = 1;
        assertThrows(IllegalArgumentException.class, () -> service.ajouterLigne(commandeNum, produitRef, quantite));
    }

    @Test
    void testAjoutProdInconnu() {
        Integer commandeNum = NUMERO_COMMANDE_PAS_LIVREE;
        Integer produitRef = REFERENCE_PRODUIT_INEXISTANTE;
        int quantite = 1;

        assertEquals(Optional.empty(), produitDao.findById(produitRef));
        assertNotNull(commandeDao.findById(commandeNum));
        assertThrows(NoSuchElementException.class, () -> service.ajouterLigne(commandeNum, produitRef, quantite));
    }

    @Test
    void testAjoutCommInconnue() {
        Integer commandeNum = NUMERO_COMMANDE_INEXISTANTE;
        Integer produitRef = REFERENCE_PRODUIT_DISPONIBLE_1;
        int quantite = 1;

        assertEquals(Optional.empty(), commandeDao.findById(commandeNum));
        assertNotNull(produitDao.findById(produitRef));
        assertThrows(NoSuchElementException.class, () -> service.ajouterLigne(commandeNum, produitRef, quantite));
    }
}
