package tn.esprit.backend.service.impl;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.backend.entity.Projet;
import tn.esprit.backend.entity.ProjetDetaille;
import tn.esprit.backend.repository.ProjetDetailleRepository;
import tn.esprit.backend.repository.ProjetRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetDetailleServiceImplTest {

    @Mock
    ProjetDetailleRepository projetDetailleRepository;
    @Mock
    ProjetRepository projetRepository;

    @InjectMocks
    ProjetDetailleServiceImpl service;

    @Test
    void addProjetDetailleDelegueAuRepository() {
        ProjetDetaille entite = new ProjetDetaille();
        when(projetDetailleRepository.save(entite)).thenReturn(entite);

        assertSame(entite, service.addProjetDetaille(entite));
        verify(projetDetailleRepository).save(entite);
    }

    @Test
    void updateProjetDetailleDelegueAuRepository() {
        ProjetDetaille entite = new ProjetDetaille();
        when(projetDetailleRepository.save(entite)).thenReturn(entite);

        assertSame(entite, service.updateProjetDetaille(entite));
        verify(projetDetailleRepository).save(entite);
    }

    @Test
    void deleteProjetDetailleSupprimeParId() {
        service.deleteProjetDetaille(1L);

        verify(projetDetailleRepository).deleteById(1L);
    }

    @Test
    void getProjetDetailleByIdRetourneLEntiteSiElleExiste() {
        ProjetDetaille entite = new ProjetDetaille();
        when(projetDetailleRepository.findById(1L)).thenReturn(Optional.of(entite));

        assertSame(entite, service.getProjetDetailleById(1L));
    }

    @Test
    void getProjetDetailleByIdRetourneNullSiAbsente() {
        when(projetDetailleRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.getProjetDetailleById(99L));
    }

    @Test
    void getAllProjetsDetaillesRetourneToutesLesEntites() {
        List<ProjetDetaille> liste = List.of(new ProjetDetaille(), new ProjetDetaille());
        when(projetDetailleRepository.findAll()).thenReturn(liste);

        assertEquals(2, service.getAllProjetsDetailles().size());
    }

    @Test
    void getProjetDetaillesByProjetFiltreParProjet() {
        when(projetDetailleRepository.findByProjetId(7L)).thenReturn(List.of(new ProjetDetaille()));

        assertEquals(1, service.getProjetDetaillesByProjet(7L).size());
    }

    @Test
    void assignProjetDetailleToProjetAssocieEtSauvegarde() {
        ProjetDetaille detail = new ProjetDetaille();
        Projet projet = new Projet();
        when(projetDetailleRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(projetRepository.findById(2L)).thenReturn(Optional.of(projet));
        when(projetDetailleRepository.save(detail)).thenReturn(detail);

        ProjetDetaille resultat = service.assignProjetDetailleToProjet(1L, 2L);

        assertSame(projet, resultat.getProjet());
    }

    @Test
    void assignProjetDetailleToProjetLeveUneExceptionSiDetailAbsent() {
        when(projetDetailleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.assignProjetDetailleToProjet(1L, 2L));
    }

    @Test
    void assignProjetDetailleToProjetLeveUneExceptionSiProjetAbsent() {
        when(projetDetailleRepository.findById(1L)).thenReturn(Optional.of(new ProjetDetaille()));
        when(projetRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.assignProjetDetailleToProjet(1L, 2L));
    }
}
