package tn.esprit.backend.service.impl;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.backend.entity.Projet;
import tn.esprit.backend.repository.ProjetRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetServiceImplTest {

    @Mock
    ProjetRepository projetRepository;

    @InjectMocks
    ProjetServiceImpl service;

    @Test
    void addProjetDelegueAuRepository() {
        Projet entite = new Projet();
        when(projetRepository.save(entite)).thenReturn(entite);

        assertSame(entite, service.addProjet(entite));
        verify(projetRepository).save(entite);
    }

    @Test
    void updateProjetDelegueAuRepository() {
        Projet entite = new Projet();
        when(projetRepository.save(entite)).thenReturn(entite);

        assertSame(entite, service.updateProjet(entite));
        verify(projetRepository).save(entite);
    }

    @Test
    void deleteProjetSupprimeParId() {
        service.deleteProjet(1L);

        verify(projetRepository).deleteById(1L);
    }

    @Test
    void getProjetByIdRetourneLEntiteSiElleExiste() {
        Projet entite = new Projet();
        when(projetRepository.findById(1L)).thenReturn(Optional.of(entite));

        assertSame(entite, service.getProjetById(1L));
    }

    @Test
    void getProjetByIdRetourneNullSiAbsente() {
        when(projetRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.getProjetById(99L));
    }

    @Test
    void getAllProjetsRetourneToutesLesEntites() {
        List<Projet> liste = List.of(new Projet(), new Projet());
        when(projetRepository.findAll()).thenReturn(liste);

        assertEquals(2, service.getAllProjets().size());
    }
}
