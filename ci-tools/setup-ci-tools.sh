#!/usr/bin/env bash

set -euo pipefail

VERT='\033[0;32m'
JAUNE='\033[1;33m'
ROUGE='\033[0;31m'
N='\033[0m'

info()  { echo -e "\n${VERT}==> $*${N}"; }
avert() { echo -e "${JAUNE}[!] $*${N}"; }
erreur(){ echo -e "${ROUGE}[X] $*${N}" >&2; }

NEW_PASSWORD="${1:-}"

if [ -z "$NEW_PASSWORD" ]; then
    erreur "Usage : $0 <nouveau_mot_de_passe_admin>"
    exit 1
fi

SONAR="http://localhost:9000"

cd "$(dirname "$0")"

info "0/6 Verification des ressources de la VM"

MEM_MB=$(awk '/MemTotal/ {printf "%d", $2/1024}' /proc/meminfo)

echo "RAM totale : ${MEM_MB} Mo"

if [ "$MEM_MB" -lt 3500 ]; then
    avert "Moins de 3,5 Go de RAM."
    avert "Augmente vb.memory dans le Vagrantfile (6144 recommande)."
fi


info "1/6 Parametres noyau requis par SonarQube"

sudo sysctl -w vm.max_map_count=524288 >/dev/null
sudo sysctl -w fs.file-max=131072 >/dev/null

printf 'vm.max_map_count=524288\nfs.file-max=131072\n' |
sudo tee /etc/sysctl.d/99-sonarqube.conf >/dev/null

echo "vm.max_map_count = $(sysctl -n vm.max_map_count)"

command -v jq >/dev/null || {
    info "Installation de jq"
    sudo apt-get update
    sudo apt-get install -y jq
}


info "2/6 Demarrage de SonarQube, PostgreSQL et Registry"

docker compose up -d

docker compose ps


info "3/6 Attente de SonarQube"

for i in $(seq 1 80); do

    STATUS=$(curl -s "${SONAR}/api/system/status" 2>/dev/null |
             jq -r '.status' 2>/dev/null || true)

    echo "[$i/80] statut : ${STATUS:-injoignable}"

    [ "$STATUS" = "UP" ] && break

    sleep 5
done


if [ "${STATUS:-}" != "UP" ]; then

    erreur "SonarQube n'est pas demarre."

    echo "Diagnostic :"
    echo "docker compose logs sonarqube --tail=50"

    exit 1
fi


info "4/6 Configuration du mot de passe administrateur"

if curl -sf \
    -u "admin:${NEW_PASSWORD}" \
    "${SONAR}/api/authentication/validate" |
    jq -e '.valid == true' >/dev/null 2>&1
then

    avert "Le mot de passe est deja configure."

else

    CODE=$(curl -s \
        -o /tmp/sonar-pw.json \
        -w '%{http_code}' \
        -u admin:admin \
        -X POST \
        "${SONAR}/api/users/change_password" \
        --data-urlencode "login=admin" \
        --data-urlencode "previousPassword=admin" \
        --data-urlencode "password=${NEW_PASSWORD}")

    if [ "$CODE" = "204" ]; then

        echo "Mot de passe admin modifie."

    else

        erreur "Echec du changement de mot de passe (HTTP $CODE)"

        cat /tmp/sonar-pw.json
        echo

        erreur "Si le mot de passe a deja ete change, relance avec ce mot de passe."

        exit 1
    fi
fi


info "5/6 Creation du token Jenkins"

TOKEN_NAME="jenkins-$(date +%Y%m%d-%H%M%S)"

TOKEN=$(curl -sf \
    -u "admin:${NEW_PASSWORD}" \
    -X POST \
    "${SONAR}/api/user_tokens/generate" \
    --data-urlencode "name=${TOKEN_NAME}" |
    jq -r '.token')


if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then

    erreur "Generation du token impossible."

    exit 1
fi


info "6/6 Creation des projets SonarQube"


creer_projet() {

    local cle="$1"
    local nom="$2"
    local code

    code=$(curl -s \
        -o /dev/null \
        -w '%{http_code}' \
        -u "admin:${NEW_PASSWORD}" \
        -X POST \
        "${SONAR}/api/projects/create" \
        --data-urlencode "project=${cle}" \
        --data-urlencode "name=${nom}")

    case "$code" in

        200)
            echo "Projet cree : ${cle}"
            ;;

        400)
            echo "Projet existant : ${cle}"
            ;;

        *)
            avert "Creation de ${cle} : HTTP ${code}"
            ;;
    esac
}


creer_projet \
"gestion-projets-backend" \
"Gestion Projets - Backend (Spring Boot)"


creer_projet \
"gestion-projets-frontend" \
"Gestion Projets - Frontend (Angular)"


IP=$(hostname -I | awk '{print $2}')

[ -z "$IP" ] && IP=$(hostname -I | awk '{print $1}')


echo ""
echo "==============================================================="
echo " SONARQUBE ET REGISTRY PRETS"
echo "==============================================================="
echo ""
echo " SonarQube : http://${IP}:9000"
echo " Login     : admin"
echo ""
echo " Registry  : http://${IP}:5000/v2/_catalog"
echo ""
echo " TOKEN JENKINS"
echo ""
echo " ${TOKEN}"
echo ""
echo " COPIE CE TOKEN MAINTENANT."
echo ""
echo " Jenkins :"
echo " Administrer Jenkins"
echo " > Credentials"
echo " > System"
echo " > Global credentials"
echo " > Add Credentials"
echo ""
echo " Kind   : Secret text"
echo " Secret : token ci-dessus"
echo " ID     : sonar-token"
echo ""
echo "==============================================================="
