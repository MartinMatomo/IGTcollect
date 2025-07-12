# Personnalisation d'IGT Collect pour l'Inspection Générale du Travail

Ce document décrit les modifications apportées à l'application IGT Collect pour la personnaliser pour l'Inspection Générale du Travail.

## Modifications apportées

### 1. Identité visuelle

- **Logo personnalisé**: Un logo IGT a été créé dans `collect_app/src/main/res/drawable/igt_logo.xml`
- **Palette de couleurs**: Une palette de couleurs spécifique à l'IGT a été définie dans `collect_app/src/main/res/values/colors_igt.xml`
- **Couleurs principales**: Les couleurs principales de l'application ont été modifiées pour utiliser les couleurs IGT
- **Support du thème sombre**: Des versions sombres des couleurs et styles ont été créées pour maintenir l'harmonie avec le thème sombre de l'application

### 2. Interface utilisateur

- **Page d'accueil**: La page d'accueil a été modifiée pour inclure le logo IGT et un titre
- **Boutons**: Les boutons du menu principal ont été redessinés avec un style spécifique à l'IGT
- **Bouton "Remplir un nouveau formulaire"**: Ce bouton a été mis en évidence avec les couleurs de l'IGT

## Palette de couleurs IGT

### Mode clair
- **Bleu primaire**: `#1A237E` - Couleur principale de l'IGT
- **Bleu foncé**: `#0D1642` - Variation foncée pour les éléments secondaires
- **Bleu clair**: `#534BAE` - Variation claire pour les accents
- **Rouge accent**: `#C62828` - Couleur d'accent pour les éléments importants
- **Orange foncé**: `#BF360C` - Couleur secondaire pour certains éléments

### Mode sombre
- **Bleu primaire**: `#536DFE` - Couleur principale de l'IGT (plus claire pour le mode sombre)
- **Bleu moyen**: `#3D5AFE` - Variation moyenne pour les éléments secondaires
- **Bleu clair**: `#8C9EFF` - Variation claire pour les accents
- **Rouge accent**: `#FF5252` - Couleur d'accent pour les éléments importants
- **Orange vif**: `#FF6E40` - Couleur secondaire pour certains éléments

## Fichiers modifiés

### Mode clair
1. `collect_app/src/main/res/drawable/igt_logo.xml` (nouveau)
2. `collect_app/src/main/res/values/colors_igt.xml` (nouveau)
3. `collect_app/src/main/res/values/colors.xml`
4. `collect_app/src/main/res/layout/main_menu.xml`
5. `collect_app/src/main/res/drawable/main_menu_button_background_igt.xml` (nouveau)
6. `collect_app/src/main/res/layout/main_menu_button.xml`
7. `collect_app/src/main/res/drawable/start_new_form_button_background.xml`
8. `collect_app/src/main/res/layout/start_new_from_button.xml`

### Mode sombre
9. `collect_app/src/main/res/values-night/colors_igt.xml` (nouveau)
10. `collect_app/src/main/res/values-night/colors.xml`
11. `collect_app/src/main/res/drawable-night/main_menu_button_background_igt.xml` (nouveau)
12. `collect_app/src/main/res/drawable-night/start_new_form_button_background.xml` (nouveau)

## Utilisation

Ces modifications sont purement esthétiques et n'affectent pas les fonctionnalités de l'application. Elles permettent simplement de personnaliser l'apparence de l'application pour l'Inspection Générale du Travail.

L'application prend en charge automatiquement le passage entre le mode clair et le mode sombre en fonction des paramètres du système Android.