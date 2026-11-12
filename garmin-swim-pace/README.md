# Swim Pace – Connect IQ data field ל-Fenix 8

שדה נתונים שרץ בתוך פעילויות השחייה המובנות של Garmin (Pool Swim / Open Water Swim), כך שספירת בריכות, GPS ושמירת הפעילות נשארים כרגיל.

## מצבים
| מצב | מתי | מה קורה |
|---|---|---|
| **Tempo** | בריכה (אוטומטי) | רטט כל בריכה (או כל N בריכות) בזמן היעד, כמו Tempo Trainer Mode 1. ב-1:30/100 ובבריכה של 25 מ': כל 22.5 שנ'. מוצג: שניות עד הרטט הבא, ופער במטרים (מתעדכן בקיר). |
| **Ghost** | ים (אוטומטי) | שוחה וירטואלי בקצב היעד. כל 100 מ' של ה"רוח": **רטט קצר** = בקצב, **2 רטטים** = מפגר, **רטט ארוך** = מהיר מדי. מוצג: פער במטרים ובשניות. |

**דיוק:** השדה מתעדכן פעם בשנייה, לכן כל רטט יכול לאחר בעד שנייה. הזמנים מחושבים מתחילת הפעילות, ולכן אין סטייה מצטברת: לאורך 4 ק"מ הקצב נשאר בדיוק 1:30.

## הגדרות (מהטלפון: Connect IQ app → My Device → Swim Pace → Settings)
- קצב ל-100 מ': דקות + שניות (אפשר גם 27.5)
- מצב: Auto / Tempo / Ghost
- אורך בריכה (ברירת מחדל 25)
- Tempo: כמה בריכות בין רטטים
- Ghost: כל כמה מטרים לקבל משוב (100), וטולרנס במטרים (15)

## בנייה והתקנה
1. התקן את [Connect IQ SDK](https://developer.garmin.com/connect-iq/sdk/) ואת התוסף **Monkey C** ל-VS Code. צור developer key (`Monkey C: Generate a Developer Key`).
2. פתח את התיקייה `garmin-swim-pace` ב-VS Code.
3. בדיקה בסימולטור: `Monkey C: Run` ובחר fenix 8. בסימולטור: Simulation → Activity Data כדי להזרים נתונים.
4. בנייה לשעון: `Monkey C: Build for Device` ובחר את הדגם שלך. נוצר קובץ `.prg`.
5. חבר את השעון בכבל USB והעתק את ה-`.prg` לתיקייה `GARMIN/APPS`.
6. בשעון: Open Water Swim (או Pool Swim) → Settings → Data Screens → בחר מסך → שדה → **Connect IQ → Swim Pace**.
7. ודא שהרטט מופעל: System → Sounds & Alerts → Vibration.

> אם הקומפיילר מתלונן על product id, ערוך את `manifest.xml` לפי הדגם המדויק שלך (`Monkey C: Edit Products`).
