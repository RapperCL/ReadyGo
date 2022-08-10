git config --global url."https://${DEPLOY_USERNAME}:${DEPLOY_PASSWORD}@git.sublive.app/".insteadOf "https://git.sublive.app/"
git clone https://git.sublive.app/sa/maven-deploy.git  maven
cp -Rf maven/settings.xml ~/.m2/
mvn clean deploy
