#!/usr/bin/env bash
set -uo pipefail

: "${SONAR_HOST_URL:?SONAR_HOST_URL non defini}"
: "${SONAR_TOKEN:?SONAR_TOKEN non defini}"

PUBLIC_URL="${SONAR_PUBLIC_URL:-$SONAR_HOST_URL}"
MAX_WAIT="${SONAR_WAIT_SECONDS:-300}"

METRICS="ncloc,bugs,vulnerabilities,security_hotspots,code_smells,coverage,duplicated_lines_density,reliability_rating,security_rating,sqale_rating,alert_status"

api() {
    curl -sS --max-time 30 -u "${SONAR_TOKEN}:" "${SONAR_HOST_URL}$1"
}

cmd_dashboard() {
    local key="$1" titre="$2" out="${3:-}"
    local mesures analyses etat date_analyse

    mesures=$(api "/api/measures/component?component=${key}&metricKeys=${METRICS}") || mesures='{}'

    {
        echo "======================================================================"
        echo "  SONARQUBE - ${titre}"
        echo "======================================================================"
        echo "  Projet    : ${key}"
        echo "  Dashboard : ${PUBLIC_URL}/dashboard?id=${key}"

        if jq -e '.errors' >/dev/null 2>&1 <<<"$mesures"; then
            echo "  Etat      : PROJET INEXISTANT dans SonarQube (aucune analyse)"
            echo "======================================================================"
        elif [ "$(jq '.component.measures | length' <<<"$mesures" 2>/dev/null)" = "0" ]; then
            echo "  Etat      : projet cree mais JAMAIS ANALYSE (dashboard vide)"
            echo "======================================================================"
        else
            analyses=$(api "/api/project_analyses/search?project=${key}&ps=1") || analyses='{}'
            date_analyse=$(jq -r '.analyses[0].date // "inconnue"' <<<"$analyses" 2>/dev/null)
            etat="derniere analyse : ${date_analyse}"

            echo "  Etat      : ${etat}"
            echo "----------------------------------------------------------------------"

            jq -r '
              def note: {"1.0":"A","2.0":"B","3.0":"C","4.0":"D","5.0":"E"}[.] // .;
              (.component.measures | map({(.metric): .value}) | add) as $m
              | [
                  ["Lignes de code",            ($m.ncloc // "-")],
                  ["Bugs",                      ($m.bugs // "-")],
                  ["Vulnerabilites",            ($m.vulnerabilities // "-")],
                  ["Security hotspots",         ($m.security_hotspots // "-")],
                  ["Code smells",               ($m.code_smells // "-")],
                  ["Couverture de tests (%)",   ($m.coverage // "-")],
                  ["Duplication (%)",           ($m.duplicated_lines_density // "-")],
                  ["Fiabilite (note)",          (($m.reliability_rating // "-") | note)],
                  ["Securite (note)",           (($m.security_rating // "-") | note)],
                  ["Maintenabilite (note)",     (($m.sqale_rating // "-") | note)]
                ][]
              | "  \(.[0] + ":" | . + (" " * (28 - length))) \(.[1])"
            ' <<<"$mesures"

            echo "----------------------------------------------------------------------"

            api "/api/qualitygates/project_status?projectKey=${key}" | jq -r '
              .projectStatus as $p
              | "  QUALITY GATE : \($p.status // "inconnu")",
                ($p.conditions // [] | .[]
                  | "     [\(.status)] \(.metricKey) = \(.actualValue // "-")  (seuil \(.errorThreshold // "-"))")'

            echo "======================================================================"
        fi
    } | if [ -n "$out" ]; then tee "$out"; else cat; fi
}

cmd_wait() {
    local key="$1"
    local deadline=$((SECONDS + MAX_WAIT))
    local reponse en_attente statut

    echo "Attente du traitement serveur de l'analyse (${key})..."

    while [ "$SECONDS" -lt "$deadline" ]; do
        reponse=$(api "/api/ce/component?component=${key}") || reponse='{}'
        en_attente=$(jq -r '.queue | length' <<<"$reponse" 2>/dev/null || true)

        if [ "$en_attente" = "0" ]; then
            statut=$(jq -r '.current.status // "AUCUN"' <<<"$reponse")

            case "$statut" in
                SUCCESS)
                    echo "Analyse traitee par SonarQube : SUCCESS"
                    return 0
                    ;;
                *)
                    echo "[X] Traitement serveur termine avec le statut : ${statut}"
                    return 1
                    ;;
            esac
        fi

        sleep 3
    done

    echo "[X] Delai depasse (${MAX_WAIT}s)"
    return 1
}

cmd_gate() {
    local key="$1"
    local reponse statut

    reponse=$(api "/api/qualitygates/project_status?projectKey=${key}") || reponse='{}'
    statut=$(jq -r '.projectStatus.status // "INCONNU"' <<<"$reponse" 2>/dev/null)

    echo "----------------------------------------------------------------------"
    echo "  QUALITY GATE (${key}) : ${statut}"

    jq -r '
        .projectStatus.conditions // [] | .[]
        | select(.status != "OK")
        | "     CONDITION EN ECHEC : \(.metricKey) = \(.actualValue // "-")  (seuil \(.errorThreshold // "-"))"
    ' <<<"$reponse" 2>/dev/null

    echo "----------------------------------------------------------------------"

    case "$statut" in
        OK)
            echo "  >>> Quality Gate REUSSI : le code est autorise a continuer."
            return 0
            ;;
        ERROR)
            echo "  >>> Quality Gate ECHOUE : la livraison est BLOQUEE."
            echo "  >>> Details : ${PUBLIC_URL}/dashboard?id=${key}"
            return 1
            ;;
        *)
            echo "  >>> Statut Quality Gate inexploitable (${statut})."
            return 2
            ;;
    esac
}

case "${1:-}" in
    dashboard)
        [ $# -ge 3 ] || {
            echo "usage: $0 dashboard <cle> <titre> [fichier]"
            exit 64
        }
        cmd_dashboard "$2" "$3" "${4:-}"
        ;;

    wait)
        [ $# -eq 2 ] || {
            echo "usage: $0 wait <cle>"
            exit 64
        }
        cmd_wait "$2"
        ;;

    gate)
        [ $# -eq 2 ] || {
            echo "usage: $0 gate <cle>"
            exit 64
        }
        cmd_gate "$2"
        ;;

    *)
        echo "usage: $0 {dashboard|wait|gate}"
        exit 64
        ;;
esac
