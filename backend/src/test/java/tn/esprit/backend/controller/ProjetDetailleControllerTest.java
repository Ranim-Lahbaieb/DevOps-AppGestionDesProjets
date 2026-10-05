package tn.esprit.backend.controller;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.backend.entity.ProjetDetaille;
import tn.esprit.backend.service.IProjetDetailleService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetDetailleControllerTest {

    @Mock
    IProjetDetailleService projetDetailleService;

    @InjectMocks
    ProjetDetailleController controller;

    @Test
    void addProjetDetailleDelegueAuService() {
        ProjetDetaille entite = new ProjetDetaille();
        when(projetDetailleService.addProjetDetaille(entite)).thenReturn(entite);

        assertSame(entite, controller.addProjetDetaille(entite));
    }

    @Test
    void updateProjetDetailleDelegueAuService() {
        ProjetDetaille entite = new ProjetDetaille();
        when(projetDetailleService.updateProjetDetaille(entite)).thenReturn(entite);

        assertSame(entite, controller.updateProjetDetaille(entite));
    }

    @Test
    void deleteProjetDetailleDelegueAuService() {
        controller.deleteProjetDetaille(4L);

        verify(projetDetailleService).deleteProjetDetaille(4L);
    }

    @Test
    void getProjetDetailleByIdDelegueAuService() {
        ProjetDetaille entite = new ProjetDetaille();
        when(projetDetailleService.getProjetDetailleById(4L)).thenReturn(entite);

        assertSame(entite, controller.getProjetDetailleById(4L));
    }

    @Test
    void getAllProjetsDetaillesDelegueAuService() {
        List<ProjetDetaille> liste = List.of(new ProjetDetaille());
        when(projetDetailleService.getAllProjetsDetailles()).thenReturn(liste);

        assertSame(liste, controller.getAllProjetsDetailles());
    }

    @Test
    void getProjetDetaillesByProjetDelegueAuService() {
        List<ProjetDetaille> liste = List.of(new ProjetDetaille());
        when(projetDetailleService.getProjetDetaillesByProjet(7L)).thenReturn(liste);

        assertSame(liste, controller.getProjetDetaillesByProjet(7L));
    }

    @Test
    void assignProjetDetailleToProjetDelegueAuService() {
        ProjetDetaille detail = new ProjetDetaille();
        when(projetDetailleService.assignProjetDetailleToProjet(1L, 2L)).thenReturn(detail);

        assertSame(detail, controller.assignProjetDetailleToProjet(1L, 2L));
    }
}
