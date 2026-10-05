package tn.esprit.backend.controller;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.backend.entity.Entreprise;
import tn.esprit.backend.service.IEntrepriseService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntrepriseControllerTest {

    @Mock
    IEntrepriseService entrepriseService;

    @InjectMocks
    EntrepriseController controller;

    @Test
    void addEntrepriseDelegueAuService() {
        Entreprise entite = new Entreprise();
        when(entrepriseService.addEntreprise(entite)).thenReturn(entite);

        assertSame(entite, controller.addEntreprise(entite));
    }

    @Test
    void updateEntrepriseDelegueAuService() {
        Entreprise entite = new Entreprise();
        when(entrepriseService.updateEntreprise(entite)).thenReturn(entite);

        assertSame(entite, controller.updateEntreprise(entite));
    }

    @Test
    void deleteEntrepriseDelegueAuService() {
        controller.deleteEntreprise(4L);

        verify(entrepriseService).deleteEntreprise(4L);
    }

    @Test
    void getEntrepriseByIdDelegueAuService() {
        Entreprise entite = new Entreprise();
        when(entrepriseService.getEntrepriseById(4L)).thenReturn(entite);

        assertSame(entite, controller.getEntrepriseById(4L));
    }

    @Test
    void getAllEntreprisesDelegueAuService() {
        List<Entreprise> liste = List.of(new Entreprise());
        when(entrepriseService.getAllEntreprises()).thenReturn(liste);

        assertSame(liste, controller.getAllEntreprises());
    }
}
