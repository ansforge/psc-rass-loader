# ACTIONS DE DEBUG
Ce document a pour vocation de lister quelques actions envisageables lors d'opérations d'investigation.

## Contrôle de la heap Java
Il est arrivé que le conteneur pscload subisse un OOM killed Nomad (Allocation mémoire excédée).
Il s'est systématiquement agi jusqu'ici d'une conséquence de l'inflation de la taille de l'extract RASS entrant, nécéssitant un retaillage de la JVM du conteneur.

Il est possible de contrôler le contenu du tas de la JVM en effectuant un dump avant destruction du conteneur, en ajoutant ces paramètres aux JAVA_TOOL_OPTIONS passées dans le template Nomad.
```bash
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/files-repo/
```

Attention, ce dump serait très volumineux et de nature à consommer une grande partie du volume portworx associé au conteneur.