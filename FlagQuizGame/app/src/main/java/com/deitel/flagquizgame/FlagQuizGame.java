// FlagQuizGame.java
// Main Activity for the Flag Quiz Game App
package com.deitel.flagquizgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class FlagQuizGame extends Activity
{
   // String used when logging error messages
   private static final String TAG = "FlagQuizGame Activity";

   private List<String> fileNameList; // flag file names
   private List<String> quizCountriesList; // names of countries in quiz
   private Map<String, Boolean> regionsMap; // which regions are enabled
   private String correctAnswer; // correct country for the current flag
   private int totalGuesses; // number of guesses made
   private int correctAnswers; // number of correct guesses
   private int guessRows; // number of rows displaying choices
   private int totalFirstGuesses = 0; // number of first try guesses
   private int firstGuess = 0; // keeps track of guess number in current flag
   private int score = 0;
   private Random random; // random number generator
   private Handler handler; // used to delay loading next flag
   private Animation shakeAnimation; // animation for incorrect guess

   private TextView answerTextView; // displays Correct! or Incorrect!
   private TextView questionNumberTextView; // shows current question #
   private TextView guessText;
   private ImageView flagImageView; // displays a flag
   private TableLayout buttonTableLayout; // table of answer Buttons
   InputStream inputStream;
   String[] csv; // csv file array
   String[] country = new String[195]; // country list array
   String[] capital = new String[195]; // capital list array
   int i = 0;
   int store,take,count; // store returns index of country, take is capital city index, count looks for country if it has capital or not
   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState); // call the superclass's method
      setContentView(R.layout.main); // inflate the GUI

      fileNameList = new ArrayList<String>(); // list of image file names
      quizCountriesList = new ArrayList<String>(); // flags in this quiz
      regionsMap = new HashMap<String, Boolean>(); // HashMap of regions
      guessRows = 1; // default to one row of choices
      random = new Random(); // initialize the random number generator
      handler = new Handler(); // used to perform delayed operations

      // load the shake animation that's used for incorrect answers
      shakeAnimation =
         AnimationUtils.loadAnimation(this, R.anim.incorrect_shake);
      shakeAnimation.setRepeatCount(3); // animation repeats 3 times

      //get csv file
      inputStream=getResources().openRawResource(R.raw.capital);
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      try {
          String csvLine;
          while ((csvLine = reader.readLine()) != null) {

              csv = csvLine.split(",");
              country[i] = csv[0];
              capital[i] = csv[1];
              try {

                  Log.e("Column 1 ", "" + csv[0]);


              } catch (Exception e) {
                  Log.e("Unknown", e.toString());
              }
              i = i + 1;
          }
      }catch(Exception e){
         Log.e("Error",e.toString());
      }
      // get array of world regions from strings.xml
      String[] regionNames =
         getResources().getStringArray(R.array.regionsList);

      // by default, countries are chosen from all regions
      for (String region : regionNames )
         regionsMap.put(region, true);

      // get references to GUI components
      questionNumberTextView =
         (TextView) findViewById(R.id.questionNumberTextView);
      flagImageView = (ImageView) findViewById(R.id.flagImageView);
      guessText = (TextView) findViewById(R.id.guessCountryTextView);
      buttonTableLayout =
         (TableLayout) findViewById(R.id.buttonTableLayout);
      answerTextView = (TextView) findViewById(R.id.answerTextView);

      // set questionNumberTextView's text
      questionNumberTextView.setText(
         getResources().getString(R.string.question) + " 1 " +
         getResources().getString(R.string.of) + " 10");

      resetQuiz(); // start a new quiz
   } // end method onCreate

   // set up and start the next quiz
   private void resetQuiz()
   {
      firstGuess = 0;
      totalFirstGuesses = 0;
      score = 0;
      // use the AssetManager to get the image flag
      // file names for only the enabled regions
      AssetManager assets = getAssets(); // get the app's AssetManager
      fileNameList.clear(); // empty the list

      try
      {
         Set<String> regions = regionsMap.keySet(); // get Set of regions

         // loop through each region
         for (String region : regions)
         {
            if (regionsMap.get(region)) // if region is enabled
            {
               // get a list of all flag image files in this region
               String[] paths = assets.list(region);

               for (String path : paths)
                  fileNameList.add(path.replace(".png", ""));
            } // end if
         } // end for
      } // end try
      catch (IOException e)
      {
         Log.e(TAG, "Error loading image file names", e);
      } // end catch

      correctAnswers = 0; // reset the number of correct answers made
      totalGuesses = 0; // reset the total number of guesses the user made
      quizCountriesList.clear(); // clear prior list of quiz countries

      // add 10 random file names to the quizCountriesList
      int flagCounter = 1;
      int numberOfFlags = fileNameList.size(); // get number of flags

      while (flagCounter <= 10)
      {
         int randomIndex = random.nextInt(numberOfFlags); // random index

         // get the random file name
         String fileName = fileNameList.get(randomIndex);

         // if the region is enabled and it hasn't already been chosen
         if (!quizCountriesList.contains(fileName))
         {
            quizCountriesList.add(fileName); // add the file to the list
            ++flagCounter;
         } // end if
      } // end while

      loadNextFlag(); // start the quiz by loading the first flag
   } // end method resetQuiz

    // gives 3 points to first guess, 2 points to second guess, 1 point to third guess
   private void scoreTracker(int guessNum)
   {
      if(correctAnswers != 0){
         switch(guessNum){
            case 1: //1st guess
               score=score+100;
               break;
            case 0: //2nd guess
               score=score+90;
               break;
            case -1: //3rd guess
               score=score+80;
               break;
            case -2: //4th guess
               score=score+70;
               break;
            case -3: //5th guess
               score=score+60;
               break;
            case -4: //6th guess
               score=score+50;
               break;
            case -5: //7th guess
               score=score+40;
               break;
            case -6: //8th guess
               score=score+30;
               break;
            case -7: //9th guess
               score=score+20;
               break;
            case 10: // Bonus Question
               score=score+50;
               break;
         }
      }
   }

   // after the user guesses a correct flag, load the next flag
   private void loadNextFlag()
   {
      scoreTracker(firstGuess);
      firstGuess=0;
      flagImageView.setVisibility(View.VISIBLE); // make flag image visible
      guessText.setText(getResources().getString(R.string.guess_country)); // guess country
      // get file name of the next flag and remove it from the list
      String nextImageName = quizCountriesList.remove(0);
      correctAnswer = nextImageName; // update the correct answer

      answerTextView.setText(""); // clear answerTextView

      // display the number of the current question in the quiz
      questionNumberTextView.setText(
         getResources().getString(R.string.question) + " " +
         (correctAnswers + 1) + " " +
         getResources().getString(R.string.of) + " 10");

      // extract the region from the next image's name
      String region =
         nextImageName.substring(0, nextImageName.indexOf('-'));

      // use AssetManager to load next image from assets folder
      AssetManager assets = getAssets(); // get app's AssetManager
      InputStream stream; // used to read in flag images

      try
      {
         // get an InputStream to the asset representing the next flag
         stream = assets.open(region + "/" + nextImageName + ".png");

         // load the asset as a Drawable and display on the flagImageView
         Drawable flag = Drawable.createFromStream(stream, nextImageName);
         flagImageView.setImageDrawable(flag);

      } // end try
      catch (IOException e)
      {
         Log.e(TAG, "Error loading " + nextImageName, e);
      } // end catch

      // clear prior answer Buttons from TableRows
      for (int row = 0; row < buttonTableLayout.getChildCount(); ++row)
         ((TableRow) buttonTableLayout.getChildAt(row)).removeAllViews();



      Collections.shuffle(fileNameList); // shuffle file names

      // put the correct answer at the end of fileNameList
      int correct = fileNameList.indexOf(correctAnswer);
      fileNameList.add(fileNameList.remove(correct));

      // get a reference to the LayoutInflater service
      LayoutInflater inflater = (LayoutInflater) getSystemService(
         Context.LAYOUT_INFLATER_SERVICE);

      // add 3, 6, or 9 answer Buttons based on the value of guessRows
      for (int row = 0; row < guessRows; row++)
      {
         TableRow currentTableRow = getTableRow(row);

         // place Buttons in currentTableRow
         for (int column = 0; column < 3; column++)
         {
            // inflate guess_button.xml to create new Button
            Button newGuessButton =
               (Button) inflater.inflate(R.layout.guess_button, null);

            // get country name and set it as newGuessButton's text
            String fileName = fileNameList.get((row * 3) + column);
            newGuessButton.setText(getCountryName(fileName));

            // register answerButtonListener to respond to button clicks
            newGuessButton.setOnClickListener(guessButtonListener);
            currentTableRow.addView(newGuessButton);
         } // end for
      } // end for

      // randomly replace one Button with the correct answer
      int row = random.nextInt(guessRows); // pick random row
      int column = random.nextInt(3); // pick random column
      TableRow randomTableRow = getTableRow(row); // get the TableRow
      String countryName = getCountryName(correctAnswer);
      ((Button)randomTableRow.getChildAt(column)).setText(countryName);
   } // end method loadNextFlag

    // after the user guesses a correct flag, load the next flag
    private void loadBonus()
    {
        answerTextView.setText(""); // clear answerTextView

        // display the number of the current question in the quiz
        questionNumberTextView.setText(
                getResources().getString(R.string.bonus));

        guessText.setText(getResources().getString(R.string.guess_capital));

        // set bonus image in bonus question
        flagImageView.setImageResource(R.drawable.bonus);

        // clear prior answer Buttons from TableRows
        for (int row = 0; row < buttonTableLayout.getChildCount(); ++row)
            ((TableRow) buttonTableLayout.getChildAt(row)).removeAllViews();

       String[] capital_copy = new String[195];
       System.arraycopy( capital, 0, capital_copy, 0, capital.length ); //copy of capital array
       List<String> capitalList = new ArrayList<String>(Arrays.asList(capital_copy));// shuffle the clone array
       Collections.shuffle(capitalList); // shuffle capital city names

       // put the correct answer at the end of capitalList
       int correct = capitalList.indexOf(capital[take]);
       capitalList.add(capitalList.remove(correct));


        // get a reference to the LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        // add 3, 6, or 9 answer Buttons based on the value of guessRows
        for (int row = 0; row < guessRows; row++)
        {
            TableRow currentTableRow = getTableRow(row);

            // place Buttons in currentTableRow
            for (int column = 0; column < 3; column++)
           {
              // inflate guess_button.xml to create new Button
              Button newGuessButton =
                      (Button) inflater.inflate(R.layout.guess_button, null);

              // Randomly choose capital city names
              newGuessButton.setText(capitalList.get((row * 3) + column));

              // register answerButtonListener to respond to button clicks
              newGuessButton.setOnClickListener(guessButtonListener);
              currentTableRow.addView(newGuessButton);
           } // end for
        } // end for

        // randomly replace one Button with the correct answer
        int row = random.nextInt(guessRows); // pick random row
        int column = random.nextInt(3); // pick random column
        TableRow randomTableRow = getTableRow(row); // get the TableRow
        String capitalName = getCountryName(capital[take]); //true answer
        ((Button)randomTableRow.getChildAt(column)).setText(capitalName);
    } // end method loadNextFlag


    // returns the specified TableRow
   private TableRow getTableRow(int row)
   {
      return (TableRow) buttonTableLayout.getChildAt(row);
   } // end method getTableRow

   // parses the country flag file name and returns the country name
   private String getCountryName(String name)
   {
      return name.substring(name.indexOf('-') + 1).replace('_', ' ');
   } // end method getCountryName

   private void submitBonus(Button guessBonusButton){
      String guess = guessBonusButton.getText().toString();
      String answer = capital[take];
      if (guess.equals(answer))
      {
         // display "Correct!" in green text
         answerTextView.setText(capital[take] + "!" + "\n+50 points");
         answerTextView.setTextColor(
                 getResources().getColor(R.color.correct_answer));

         scoreTracker(10);

         disableButtons(); // disable all answer Buttons
         // if the game is not end
         if(correctAnswers != 10) {
            handler.postDelayed(
                    new Runnable() {
                       @Override
                       public void run() {
                          loadNextFlag();
                       }
                    }, 1000); // 1000 milliseconds for 1-second delay
         }else{   // if the answer is true and game is end call the dialog
            dialogCaller();
         }
      } // end if
      else // guess was incorrect
      {
         // play the animation
         // flagImageView.startAnimation(shakeAnimation);
         // display "Incorrect!" in red
         answerTextView.setText(capital[take] + "!");
         answerTextView.setTextColor(
                 getResources().getColor(R.color.incorrect_answer));

         // if the game is not end
         if(correctAnswers != 10) {
            handler.postDelayed(
                    new Runnable() {
                       @Override
                       public void run() {
                          loadNextFlag();
                       }
                    }, 1000); // 1000 milliseconds for 1-second delay
         }else{ // if the answer is true and game is end call the dialog
             dialogCaller();
         }
      } // end else
   }

   // called when the user selects an answer
   private void submitGuess(Button guessButton)
   {
      String guess = guessButton.getText().toString();
      String answer = getCountryName(correctAnswer);

      take = findCapital(answer);

      ++totalGuesses; // increment the number of guesses the user has made

      // if the guess is correct
      if (guess.equals(answer))
      {
         firstGuess++;
         ++correctAnswers; // increment the number of correct answers

         // display "Correct!" in green text
         answerTextView.setText(answer + "!");
         answerTextView.setTextColor(
            getResources().getColor(R.color.correct_answer));

         disableButtons(); // disable all answer Buttons

         // if the user has correctly identified 10 flags
         if (correctAnswers == 10)
         {
             scoreTracker(firstGuess);
            if(firstGuess == 1) {
               totalFirstGuesses++;
               handler.postDelayed(
                  new Runnable()
                  {
                    @Override
                    public void run()
                    {
                     loadBonus();
                    }
                  }, 1000); // 1000 milliseconds for 1-second delay // load the bonus question
            }else { // call dialog and end the game
               dialogCaller();
            }
         } // end if
         else // answer is correct but quiz is not over
         {
            // load the next flag after a 1-second delay
            handler.postDelayed(
               new Runnable()
               {
                  @Override
                  public void run()
                  {
                      if(firstGuess==1 && count==1){
                          loadBonus();
                      }else {
                          loadNextFlag();
                      }
                  }
               }, 1000); // 1000 milliseconds for 1-second delay
         } // end else
      } // end if
      else // guess was incorrect
      {
         firstGuess--;

         // play the animation
         flagImageView.startAnimation(shakeAnimation);

         // display "Incorrect!" in red
         answerTextView.setText(R.string.incorrect_answer);
         answerTextView.setTextColor(
            getResources().getColor(R.color.incorrect_answer));
         guessButton.setEnabled(false); // disable the incorrect answer
      } // end else
      if(firstGuess==1){
         totalFirstGuesses++;
      }

   } // end method submitGuess

   // dialog screen at the end of game
   public void dialogCaller(){
       // create a new AlertDialog Builder
       AlertDialog.Builder builder = new AlertDialog.Builder(this);

       builder.setTitle(R.string.reset_quiz); // title bar string

       // set the AlertDialog's message to display game results
       builder.setMessage(String.format("%d %s, %.02f%% %s \n %s %d \n %s %d",
               totalGuesses, getResources().getString(R.string.guesses),
               (1000 / (double) totalGuesses),
               getResources().getString(R.string.correct),
               getResources().getString(R.string.first_try),
               totalFirstGuesses,
               "Score is: ",
               score));

       builder.setCancelable(false);

       // add "Reset Quiz" Button
       builder.setPositiveButton(R.string.reset_quiz,
               new DialogInterface.OnClickListener()
               {
                   public void onClick(DialogInterface dialog, int id)
                   {
                       resetQuiz();
                   } // end method onClick
               } // end anonymous inner class
       ); // end call to setPositiveButton

       // create AlertDialog from the Builder
       AlertDialog resetDialog = builder.create();
       resetDialog.show(); // display the Dialog
   }

   // compares the answer and finds the capital city if exists
   public int findCapital(String ans){
      count=0;
      for(i=0; i<195; i++){
          if(country[i].equals(ans)){
              store = i;
              count++;
          }
      }
      return store;
   }

   // utility method that disables all answer Buttons
   private void disableButtons()
   {
      for (int row = 0; row < buttonTableLayout.getChildCount(); ++row)
      {
         TableRow tableRow = (TableRow) buttonTableLayout.getChildAt(row);
         for (int i = 0; i < tableRow.getChildCount(); ++i)
            tableRow.getChildAt(i).setEnabled(false);
      } // end outer for
   } // end method disableButtons

   // create constants for each menu id
   private final int CHOICES_MENU_ID = Menu.FIRST;
   private final int REGIONS_MENU_ID = Menu.FIRST + 1;

   // called when the user accesses the options menu
   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
      super.onCreateOptionsMenu(menu);

      // add two options to the menu - "Choices" and "Regions"
      menu.add(Menu.NONE, CHOICES_MENU_ID, Menu.NONE, R.string.choices);
      menu.add(Menu.NONE, REGIONS_MENU_ID, Menu.NONE, R.string.regions);

      return true; // display the menu
   }  // end method onCreateOptionsMenu

   // called when the user selects an option from the menu
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      // switch the menu id of the user-selected option
      switch (item.getItemId())
      {
         case CHOICES_MENU_ID:
            // create a list of the possible numbers of answer choices
            final String[] possibleChoices =
               getResources().getStringArray(R.array.guessesList);

            // create a new AlertDialog Builder and set its title
            AlertDialog.Builder choicesBuilder =
               new AlertDialog.Builder(this);
            choicesBuilder.setTitle(R.string.choices);

            // add possibleChoices's items to the Dialog and set the
            // behavior when one of the items is clicked
            choicesBuilder.setItems(R.array.guessesList,
               new DialogInterface.OnClickListener()
               {
                  public void onClick(DialogInterface dialog, int item)
                  {
                     // update guessRows to match the user's choice
                     guessRows = Integer.parseInt(
                        possibleChoices[item].toString()) / 3;
                     resetQuiz(); // reset the quiz
                  } // end method onClick
               } // end anonymous inner class
            );  // end call to setItems

            // create an AlertDialog from the Builder
            AlertDialog choicesDialog = choicesBuilder.create();
            choicesDialog.show(); // show the Dialog
            return true;

         case REGIONS_MENU_ID:
            // get array of world regions
            final String[] regionNames =
               regionsMap.keySet().toArray(new String[regionsMap.size()]);

            // boolean array representing whether each region is enabled
            boolean[] regionsEnabled = new boolean[regionsMap.size()];
            for (int i = 0; i < regionsEnabled.length; ++i)
               regionsEnabled[i] = regionsMap.get(regionNames[i]);

            // create an AlertDialog Builder and set the dialog's title
            AlertDialog.Builder regionsBuilder =
               new AlertDialog.Builder(this);
            regionsBuilder.setTitle(R.string.regions);

            // replace _ with space in region names for display purposes
            String[] displayNames = new String[regionNames.length];
            for (int i = 0; i < regionNames.length; ++i)
               displayNames[i] = regionNames[i].replace('_', ' ');

            // add displayNames to the Dialog and set the behavior
            // when one of the items is clicked
            regionsBuilder.setMultiChoiceItems(
               displayNames, regionsEnabled,
               new DialogInterface.OnMultiChoiceClickListener()
               {
                  @Override
                  public void onClick(DialogInterface dialog, int which,
                     boolean isChecked)
                  {
                     // include or exclude the clicked region
                     // depending on whether or not it's checked
                     regionsMap.put(
                        regionNames[which].toString(), isChecked);
                  } // end method onClick
               } // end anonymous inner class
            ); // end call to setMultiChoiceItems

            // resets quiz when user presses the "Reset Quiz" Button
            regionsBuilder.setPositiveButton(R.string.reset_quiz,
               new DialogInterface.OnClickListener()
               {
                  @Override
                  public void onClick(DialogInterface dialog, int button)
                  {
                     resetQuiz(); // reset the quiz
                  } // end method onClick
               } // end anonymous inner class
            ); // end call to method setPositiveButton

            // create a dialog from the Builder
            AlertDialog regionsDialog = regionsBuilder.create();
            regionsDialog.show(); // display the Dialog
            return true;
      } // end switch

      return super.onOptionsItemSelected(item);
   } // end method onOptionsItemSelected

   // called when a guess Button is touched
   private OnClickListener guessButtonListener = new OnClickListener()
   {
      @Override
      public void onClick(View v)
      {
         // if it is the bonus question
         if(firstGuess==1){
            submitBonus((Button) v);
         }else {
            submitGuess((Button) v); // pass selected Button to submitGuess
         }
      } // end method onClick
   }; // end answerButtonListener
} // end FlagQuizGame

/*************************************************************************
* (C) Copyright 1992-2012 by Deitel & Associates, Inc. and               *
* Pearson Education, Inc. All Rights Reserved.                           *
*                                                                        *
* DISCLAIMER: The authors and publisher of this book have used their     *
* best efforts in preparing the book. These efforts include the          *
* development, research, and testing of the theories and programs        *
* to determine their effectiveness. The authors and publisher make       *
* no warranty of any kind, expressed or implied, with regard to these    *
* programs or to the documentation contained in these books. The authors *
* and publisher shall not be liable in any event for incidental or       *
* consequential damages in connection with, or arising out of, the       *
* furnishing, performance, or use of these programs.                     *
*************************************************************************/
