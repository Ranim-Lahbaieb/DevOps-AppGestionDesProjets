package tn.esprit.backend.service.impl;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.backend.entity.Entreprise;
import tn.esprit.backend.entity.Equipe;
import tn.esprit.backend.entity.Projet;
import tn.esprit.backend.repository.EntrepriseRepository;
import tn.esprit.backend.repository.EquipeRepository;
import tn.esprit.backend.repository.ProjetRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipeServiceImplTest {

    @Mock
    EquipeRepository equipeRepository;
    @Mock
    EntrepriseRepository entrepriseRepository;
    @Mock
    ProjetRepository projetRepository;

    @InjectMocks
    EquipeServiceImpl service;

    @Test
    void addEquipeDelegueAuRepository() {
        Equipe entite = new Equipe();
        when(equipeRepository.save(entite)).thenReturn(entite);

        assertSame(entite, service.addEquipe(entite));
        verify(equipeRepository).save(entite);
    }

    @Test
    void updateEquipeDelegueAuRepository() {
        Equipe entite = new Equipe();
        when(equipeRepository.save(entite)).thenReturn(entite);

        assertSame(entite, service.updateEquipe(entite));
        verify(equipeRepository).save(entite);
    }

    @Test
    void deleteEquipeSupprimeParId() {
        service.deleteEquipe(1L);

        verify(equipeRepository).deleteById(1L);
    }

    @Test
    void getEquipeByIdRetourneLEntiteSiElleExiste() {
        Equipe entite = new Equipe();
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(entite));

        assertSame(entite, service.getEquipeById(1L));
    }

    @Test
    void getEquipeByIdRetourneNullSiAbsente() {
        when(equipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.getEquipeById(99L));
    }

    @Test
    void getAllEquipesRetourneToutesLesEntites() {
        List<Equipe> liste = List.of(new Equipe(), new Equipe());
        when(equipeRepository.findAll()).thenReturn(liste);

        assertEquals(2, service.getAllEquipes().size());
    }

    @Test
    void getEquipesByEntrepriseFiltreParEntreprise() {
        when(equipeRepository.findByEntrepriseId(5L)).thenReturn(List.of(new Equipe()));

        assertEquals(1, service.getEquipesByEntreprise(5L).size());
    }

    @Test
    void assignEquipeToEntrepriseAssocieEtSauvegarde() {
        Equipe equipe = new Equipe();
        Entreprise entreprise = new Entreprise();
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));
        when(entrepriseRepository.findById(2L)).thenReturn(Optional.of(entreprise));
        when(equipeRepository.save(equipe)).thenReturn(equipe);

        Equipe resultat = service.assignEquipeToEntreprise(1L, 2L);

        assertSame(entreprise, resultat.getEntreprise());
    }

    @Test
    void assignEquipeToEntrepriseLeveUneExceptionSiEquipeAbsente() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.assignEquipeToEntreprise(1L, 2L));
    }

    @Test
    void assignEquipeToEntrepriseLeveUneExceptionSiEntrepriseAbsente() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(new Equipe()));
        when(entrepriseRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.assignEquipeToEntreprise(1L, 2L));
    }

    @Test
    void assignEquipeToProjetAjouteLeProjetALEquipe() {
        Equipe equipe = new Equipe();
        Projet projet = new Projet();
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));
        when(projetRepository.findById(3L)).thenReturn(Optional.of(projet));
        when(equipeRepository.save(equipe)).thenReturn(equipe);

        Equipe resultat = service.assignEquipeToProjet(1L, 3L);

        assertTrue(resultat.getProjets().contains(projet));
    }

    @Test
    void assignEquipeToProjetLeveUneExceptionSiProjetAbsent() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(new Equipe()));
        when(projetRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.assignEquipeToProjet(1L, 3L));
    }
}
