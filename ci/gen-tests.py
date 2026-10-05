#!/usr/bin/env python3
"""Genere les 8 classes de tests unitaires (Mockito) du backend.
Usage (a la racine du depot) :  python3 ci/gen-tests.py
Tests purs JUnit5 + Mockito : aucune base de donnees necessaire."""
import os
import re

BASE = 'backend/src/test/java/tn/esprit/backend'
ENT = ['Entreprise', 'Equipe', 'Projet', 'ProjetDetaille']
REPOS = [e + 'Repository' for e in ENT]
STATIC = {'assertSame': 'org.junit.jupiter.api.Assertions', 'assertNull': 'org.junit.jupiter.api.Assertions',
          'assertEquals': 'org.junit.jupiter.api.Assertions', 'assertThrows': 'org.junit.jupiter.api.Assertions',
          'assertTrue': 'org.junit.jupiter.api.Assertions',
          'when': 'org.mockito.Mockito', 'verify': 'org.mockito.Mockito'}


def render(pkg, cls, mocks, body):
    """Assemble la classe avec imports explicites (uniquement ceux utilises)."""
    body = re.sub(r'void (\w+)\(', lambda m: 'void ' + re.sub(r'_([a-zA-Z])', lambda x: x.group(1).upper(), m.group(1)) + '(', body)
    decl = '\n'.join(f'    @Mock\n    {t} {n};' for t, n in mocks)
    code = f'class {cls}Test {{\n\n{decl}\n\n    @InjectMocks\n    {cls} {"service" if pkg.endswith("impl") else "controller"};\n\n{body}}}\n'
    imps = {'org.junit.jupiter.api.Test', 'org.junit.jupiter.api.extension.ExtendWith', 'org.mockito.InjectMocks',
            'org.mockito.Mock', 'org.mockito.junit.jupiter.MockitoExtension'}
    imps |= {f'tn.esprit.backend.entity.{e}' for e in ENT if re.search(rf'\b{e}\b', code)}
    imps |= {f'tn.esprit.backend.repository.{r}' for r in REPOS if re.search(rf'\b{r}\b', code)}
    imps |= {f'tn.esprit.backend.service.{i}' for i in re.findall(r'\bI(?:Entreprise|Equipe|Projet|ProjetDetaille)Service\b', code)}
    if 'EntityNotFoundException' in code: imps.add('jakarta.persistence.EntityNotFoundException')
    if re.search(r'\bList\b', code): imps.add('java.util.List')
    if re.search(r'\bOptional\b', code): imps.add('java.util.Optional')
    statics = sorted({f'{c}.{n}' for n, c in STATIC.items() if re.search(rf'\b{n}\(', code)})
    head = f'package {pkg};\n\n' + '\n'.join(f'import {i};' for i in sorted(imps)) + '\n\n'
    head += '\n'.join(f'import static {s};' for s in statics) + '\n\n@ExtendWith(MockitoExtension.class)\n'
    path = os.path.join(BASE, 'service/impl' if pkg.endswith('impl') else 'controller', f'{cls}Test.java')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, 'w', encoding='utf-8').write(head + code)
    print('  +', path)


def svc_crud(E, repo, S):
    return f'''    @Test
    void add{E}_delegueAuRepository() {{
        {E} entite = new {E}();
        when({repo}.save(entite)).thenReturn(entite);

        assertSame(entite, service.add{E}(entite));
        verify({repo}).save(entite);
    }}

    @Test
    void update{E}_delegueAuRepository() {{
        {E} entite = new {E}();
        when({repo}.save(entite)).thenReturn(entite);

        assertSame(entite, service.update{E}(entite));
        verify({repo}).save(entite);
    }}

    @Test
    void delete{E}_supprimeParId() {{
        service.delete{E}(1L);

        verify({repo}).deleteById(1L);
    }}

    @Test
    void get{E}ById_retourneLEntiteSiElleExiste() {{
        {E} entite = new {E}();
        when({repo}.findById(1L)).thenReturn(Optional.of(entite));

        assertSame(entite, service.get{E}ById(1L));
    }}

    @Test
    void get{E}ById_retourneNullSiAbsente() {{
        when({repo}.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.get{E}ById(99L));
    }}

    @Test
    void getAll{S}_retourneToutesLesEntites() {{
        List<{E}> liste = List.of(new {E}(), new {E}());
        when({repo}.findAll()).thenReturn(liste);

        assertEquals(2, service.getAll{S}().size());
    }}
'''


def ctl_crud(E, svc, S):
    return f'''    @Test
    void add{E}_delegueAuService() {{
        {E} entite = new {E}();
        when({svc}.add{E}(entite)).thenReturn(entite);

        assertSame(entite, controller.add{E}(entite));
    }}

    @Test
    void update{E}_delegueAuService() {{
        {E} entite = new {E}();
        when({svc}.update{E}(entite)).thenReturn(entite);

        assertSame(entite, controller.update{E}(entite));
    }}

    @Test
    void delete{E}_delegueAuService() {{
        controller.delete{E}(4L);

        verify({svc}).delete{E}(4L);
    }}

    @Test
    void get{E}ById_delegueAuService() {{
        {E} entite = new {E}();
        when({svc}.get{E}ById(4L)).thenReturn(entite);

        assertSame(entite, controller.get{E}ById(4L));
    }}

    @Test
    void getAll{S}_delegueAuService() {{
        List<{E}> liste = List.of(new {E}());
        when({svc}.getAll{S}()).thenReturn(liste);

        assertSame(liste, controller.getAll{S}());
    }}
'''


SVC = 'tn.esprit.backend.service.impl'
CTL = 'tn.esprit.backend.controller'

# ----------------------------------------------------------------- services
render(SVC, 'EntrepriseServiceImpl', [('EntrepriseRepository', 'entrepriseRepository')],
       svc_crud('Entreprise', 'entrepriseRepository', 'Entreprises'))
render(SVC, 'ProjetServiceImpl', [('ProjetRepository', 'projetRepository')],
       svc_crud('Projet', 'projetRepository', 'Projets'))

render(SVC, 'EquipeServiceImpl',
       [('EquipeRepository', 'equipeRepository'), ('EntrepriseRepository', 'entrepriseRepository'),
        ('ProjetRepository', 'projetRepository')],
       svc_crud('Equipe', 'equipeRepository', 'Equipes') + '''
    @Test
    void getEquipesByEntreprise_filtreParEntreprise() {
        when(equipeRepository.findByEntrepriseId(5L)).thenReturn(List.of(new Equipe()));

        assertEquals(1, service.getEquipesByEntreprise(5L).size());
    }

    @Test
    void assignEquipeToEntreprise_associeEtSauvegarde() {
        Equipe equipe = new Equipe();
        Entreprise entreprise = new Entreprise();
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));
        when(entrepriseRepository.findById(2L)).thenReturn(Optional.of(entreprise));
        when(equipeRepository.save(equipe)).thenReturn(equipe);

        Equipe resultat = service.assignEquipeToEntreprise(1L, 2L);

        assertSame(entreprise, resultat.getEntreprise());
    }

    @Test
    void assignEquipeToEntreprise_leveUneExceptionSiEquipeAbsente() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.assignEquipeToEntreprise(1L, 2L));
    }

    @Test
    void assignEquipeToEntreprise_leveUneExceptionSiEntrepriseAbsente() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(new Equipe()));
        when(entrepriseRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.assignEquipeToEntreprise(1L, 2L));
    }

    @Test
    void assignEquipeToProjet_ajouteLeProjetALEquipe() {
        Equipe equipe = new Equipe();
        Projet projet = new Projet();
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));
        when(projetRepository.findById(3L)).thenReturn(Optional.of(projet));
        when(equipeRepository.save(equipe)).thenReturn(equipe);

        Equipe resultat = service.assignEquipeToProjet(1L, 3L);

        assertTrue(resultat.getProjets().contains(projet));
    }

    @Test
    void assignEquipeToProjet_leveUneExceptionSiProjetAbsent() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(new Equipe()));
        when(projetRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.assignEquipeToProjet(1L, 3L));
    }
''')

render(SVC, 'ProjetDetailleServiceImpl',
       [('ProjetDetailleRepository', 'projetDetailleRepository'), ('ProjetRepository', 'projetRepository')],
       svc_crud('ProjetDetaille', 'projetDetailleRepository', 'ProjetsDetailles') + '''
    @Test
    void getProjetDetaillesByProjet_filtreParProjet() {
        when(projetDetailleRepository.findByProjetId(7L)).thenReturn(List.of(new ProjetDetaille()));

        assertEquals(1, service.getProjetDetaillesByProjet(7L).size());
    }

    @Test
    void assignProjetDetailleToProjet_associeEtSauvegarde() {
        ProjetDetaille detail = new ProjetDetaille();
        Projet projet = new Projet();
        when(projetDetailleRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(projetRepository.findById(2L)).thenReturn(Optional.of(projet));
        when(projetDetailleRepository.save(detail)).thenReturn(detail);

        ProjetDetaille resultat = service.assignProjetDetailleToProjet(1L, 2L);

        assertSame(projet, resultat.getProjet());
    }

    @Test
    void assignProjetDetailleToProjet_leveUneExceptionSiDetailAbsent() {
        when(projetDetailleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.assignProjetDetailleToProjet(1L, 2L));
    }

    @Test
    void assignProjetDetailleToProjet_leveUneExceptionSiProjetAbsent() {
        when(projetDetailleRepository.findById(1L)).thenReturn(Optional.of(new ProjetDetaille()));
        when(projetRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.assignProjetDetailleToProjet(1L, 2L));
    }
''')

# -------------------------------------------------------------- controllers
render(CTL, 'EntrepriseController', [('IEntrepriseService', 'entrepriseService')],
       ctl_crud('Entreprise', 'entrepriseService', 'Entreprises'))
render(CTL, 'ProjetController', [('IProjetService', 'projetService')],
       ctl_crud('Projet', 'projetService', 'Projets'))
render(CTL, 'EquipeController', [('IEquipeService', 'equipeService')],
       ctl_crud('Equipe', 'equipeService', 'Equipes') + '''
    @Test
    void getEquipesByEntreprise_delegueAuService() {
        List<Equipe> liste = List.of(new Equipe());
        when(equipeService.getEquipesByEntreprise(1L)).thenReturn(liste);

        assertSame(liste, controller.getEquipesByEntreprise(1L));
    }

    @Test
    void assignEquipeToEntreprise_delegueAuService() {
        Equipe equipe = new Equipe();
        when(equipeService.assignEquipeToEntreprise(1L, 2L)).thenReturn(equipe);

        assertSame(equipe, controller.assignEquipeToEntreprise(1L, 2L));
    }

    @Test
    void assignEquipeToProjet_delegueAuService() {
        Equipe equipe = new Equipe();
        when(equipeService.assignEquipeToProjet(1L, 3L)).thenReturn(equipe);

        assertSame(equipe, controller.assignEquipeToProjet(1L, 3L));
    }
''')
render(CTL, 'ProjetDetailleController', [('IProjetDetailleService', 'projetDetailleService')],
       ctl_crud('ProjetDetaille', 'projetDetailleService', 'ProjetsDetailles') + '''
    @Test
    void getProjetDetaillesByProjet_delegueAuService() {
        List<ProjetDetaille> liste = List.of(new ProjetDetaille());
        when(projetDetailleService.getProjetDetaillesByProjet(7L)).thenReturn(liste);

        assertSame(liste, controller.getProjetDetaillesByProjet(7L));
    }

    @Test
    void assignProjetDetailleToProjet_delegueAuService() {
        ProjetDetaille detail = new ProjetDetaille();
        when(projetDetailleService.assignProjetDetailleToProjet(1L, 2L)).thenReturn(detail);

        assertSame(detail, controller.assignProjetDetailleToProjet(1L, 2L));
    }
''')
