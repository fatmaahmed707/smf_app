import 'package:flutter/material.dart';
import '../localization/app_strings.dart';

class LanguageProvider extends ChangeNotifier{

String currentLanguage="en";

void toggleLanguage(){

currentLanguage=
currentLanguage=="en"?"ar":"en";

notifyListeners();

}

String getText(String key){

return AppStrings.text[currentLanguage]![key]??key;

}

bool get isArabic=>currentLanguage=="ar";

}