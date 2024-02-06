# Eclipse Liberty Project Helper — Importer Plugin

This plugin helps Open Liberty contributors use Eclipse to develop Open Liberty.

Open Liberty has thousands of projects, and a contributor may want to work on only a few of these.
This plugin lets the user choose projects to work on, and import just the necessary projects to safely and effectively edit the code they are interested in.

## How to build the plugin

### Update the version (if there have been changes)

Update the version strings in the following files (they should all agree):

        io.openliberty.elph.feature/feature.xml
        io.openliberty.elph.plugin/META-INF/MANIFEST.MF
        io.openliberty.elph.updatesite/site.xml

### Re-build the update site

In the `io.openliberty.elph.updatesite` project, remove all the files except `site.xml`.

In Eclipse, open the `site.xml` file, go to the `Site Map` tab, and click `Build All`.

## How to install the plugin

* In the Eclipse to be updated, go to `Help` >  `Install New Software`.
* Click the `Add` button to add the update site.
* In the dialog that follows:
  * Type a meaningful name for the update site (e.g. `Elph local`).
  * Click the `Local` button and select the `io.openliberty.elph.updatesite` folder.
  * Use the dialog to locate and install the plugin, accepting the licenses and trusting content on the way.

## How to update the plugin

* Update the version (see above)
* Re-build the update site (see above)
* Restart Eclipse to refresh the update site
* Click on `Help` > `Check for updates` to find and install the updated plugin
