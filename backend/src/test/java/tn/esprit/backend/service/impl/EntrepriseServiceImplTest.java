package tn.esprit.backend.service.impl;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.backend.entity.Entreprise;
import tn.esprit.backend.repository.EntrepriseRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntrepriseServiceImplTest {

    @Mock
    EntrepriseRepository entrepriseRepository;

    @InjectMocks
    EntrepriseServiceImpl service;

    @Test
    void addEntrepriseDelegueAuRepository() {
        Entreprise entite = new Entreprise();
        when(entrepriseRepository.save(entite)).thenReturn(entite);

        assertSame(entite, service.addEntreprise(entite));
        verify(entrepriseRepository).save(entite);
    }

    @Test
    void updateEntrepriseDelegueAuRepository() {
        Entreprise entite = new Entreprise();
        when(entrepriseRepository.save(entite)).thenReturn(entite);

        assertSame(entite, service.updateEntreprise(entite));
        verify(entrepriseRepository).save(entite);
    }

    @Test
    void deleteEntrepriseSupprimeParId() {
        service.deleteEntreprise(1L);

        verify(entrepriseRepository).deleteById(1L);
    }

    @Test
    void getEntrepriseByIdRetourneLEntiteSiElleExiste() {
        Entreprise entite = new Entreprise();
        when(entrepriseRepository.findById(1L)).thenReturn(Optional.of(entite));

        assertSame(entite, service.getEntrepriseById(1L));
    }

    @Test
    void getEntrepriseByIdRetourneNullSiAbsente() {
        when(entrepriseRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.getEntrepriseById(99L));
    }

    @Test
    void getAllEntreprisesRetourneToutesLesEntites() {
        List<Entreprise> liste = List.of(new Entreprise(), new Entreprise());
        when(entrepriseRepository.findAll()).thenReturn(liste);

        assertEquals(2, service.getAllEntreprises().size());
    }
}
