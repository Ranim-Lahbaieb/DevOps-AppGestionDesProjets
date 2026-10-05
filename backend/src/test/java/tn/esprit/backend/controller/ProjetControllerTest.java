package tn.esprit.backend.controller;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.backend.entity.Projet;
import tn.esprit.backend.service.IProjetService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetControllerTest {

    @Mock
    IProjetService projetService;

    @InjectMocks
    ProjetController controller;

    @Test
    void addProjetDelegueAuService() {
        Projet entite = new Projet();
        when(projetService.addProjet(entite)).thenReturn(entite);

        assertSame(entite, controller.addProjet(entite));
    }

    @Test
    void updateProjetDelegueAuService() {
        Projet entite = new Projet();
        when(projetService.updateProjet(entite)).thenReturn(entite);

        assertSame(entite, controller.updateProjet(entite));
    }

    @Test
    void deleteProjetDelegueAuService() {
        controller.deleteProjet(4L);

        verify(projetService).deleteProjet(4L);
    }

    @Test
    void getProjetByIdDelegueAuService() {
        Projet entite = new Projet();
        when(projetService.getProjetById(4L)).thenReturn(entite);

        assertSame(entite, controller.getProjetById(4L));
    }

    @Test
    void getAllProjetsDelegueAuService() {
        List<Projet> liste = List.of(new Projet());
        when(projetService.getAllProjets()).thenReturn(liste);

        assertSame(liste, controller.getAllProjets());
    }
}
