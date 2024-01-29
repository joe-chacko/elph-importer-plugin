# Eclipse Liberty Project Helper — Importer Plugin


This plugin helps Open Liberty contributors use Eclipse to develop Open Liberty.

Open Liberty has thousands of projects, and a contributor may want to work on only a few of these.
This plugin lets the user choose projects to work on, and import just the necessary projects to safely and effectively edit the code they are interested in.

## Installing ELPH Importer Plugin
- Clone the ELPH update site locally

        git clone git@github.ibm.com:CHACKOJ/elph-update-site.git
- Open Eclipse and navigate to `Help > Install New Software`
- Within the `Work with:` text box, type the local path to the elph-update-site repo e.g. `/Users/<My Name>/Github/elph-update-site` and click the Add button
- If a pop-up loads, in the `Name` text box, give the repository a name e.g. `ELPH Local Repo` and then click the Add button
- Ensure the checkbox for the new Elph icon that appears is checked and then click Next
- Click the Finish button
- When asked `Do you trust unsigned content of unknown origin?`, click Select All, then click Trust Selected
- You will be asked to restart Eclipse for the plugin to be installed

## Running ELPH Importer Plugin
- Within Eclipse, navigate to File > Import > Elph > Import Liberty Projects

## Updating ELPH Importer Plugin
- In your terminal, change directory to the `elph-update-site` repo and run the following commands to get the latest changes

        git pull
- Restart Eclipse
- Navigate to `Help > Check For Updates`
- If there are any new updates, then Elph will appear as an option. Tick the checkbox for Elph if it appears, then click `Next` and follow the instructions on Eclipse to install the updates

