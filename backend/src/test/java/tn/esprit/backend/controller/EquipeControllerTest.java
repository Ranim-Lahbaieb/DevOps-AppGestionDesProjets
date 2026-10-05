package tn.esprit.backend.controller;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.backend.entity.Equipe;
import tn.esprit.backend.service.IEquipeService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipeControllerTest {

    @Mock
    IEquipeService equipeService;

    @InjectMocks
    EquipeController controller;

    @Test
    void addEquipeDelegueAuService() {
        Equipe entite = new Equipe();
        when(equipeService.addEquipe(entite)).thenReturn(entite);

        assertSame(entite, controller.addEquipe(entite));
    }

    @Test
    void updateEquipeDelegueAuService() {
        Equipe entite = new Equipe();
        when(equipeService.updateEquipe(entite)).thenReturn(entite);

        assertSame(entite, controller.updateEquipe(entite));
    }

    @Test
    void deleteEquipeDelegueAuService() {
        controller.deleteEquipe(4L);

        verify(equipeService).deleteEquipe(4L);
    }

    @Test
    void getEquipeByIdDelegueAuService() {
        Equipe entite = new Equipe();
        when(equipeService.getEquipeById(4L)).thenReturn(entite);

        assertSame(entite, controller.getEquipeById(4L));
    }

    @Test
    void getAllEquipesDelegueAuService() {
        List<Equipe> liste = List.of(new Equipe());
        when(equipeService.getAllEquipes()).thenReturn(liste);

        assertSame(liste, controller.getAllEquipes());
    }

    @Test
    void getEquipesByEntrepriseDelegueAuService() {
        List<Equipe> liste = List.of(new Equipe());
        when(equipeService.getEquipesByEntreprise(1L)).thenReturn(liste);

        assertSame(liste, controller.getEquipesByEntreprise(1L));
    }

    @Test
    void assignEquipeToEntrepriseDelegueAuService() {
        Equipe equipe = new Equipe();
        when(equipeService.assignEquipeToEntreprise(1L, 2L)).thenReturn(equipe);

        assertSame(equipe, controller.assignEquipeToEntreprise(1L, 2L));
    }

    @Test
    void assignEquipeToProjetDelegueAuService() {
        Equipe equipe = new Equipe();
        when(equipeService.assignEquipeToProjet(1L, 3L)).thenReturn(equipe);

        assertSame(equipe, controller.assignEquipeToProjet(1L, 3L));
    }
}
