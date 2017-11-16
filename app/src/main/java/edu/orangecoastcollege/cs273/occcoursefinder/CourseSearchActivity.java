package edu.orangecoastcollege.cs273.occcoursefinder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

/**
 * This activity will allow the user to search through all the Computer Science courses offered
 * coupled with the instructor teaching the course.
 *
 * @author Derek Tran
 * @version 1.0
 * @since November 14, 2017
 */
public class CourseSearchActivity extends AppCompatActivity
{

    private DBHelper db;
    private static final String TAG = "OCC Course Finder";

    /**
     * Initializes <code>CourseSearchActivity</code> by inflating its UI.
     *
     * @param savedInstanceState Bundle containing the data it recently supplied in
     *                           onSaveInstanceState(Bundle) if activity was reinitialized after
     *                           being previously shut down. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);

        deleteDatabase(DBHelper.DATABASE_NAME);
        db = new DBHelper(this);
        db.importCoursesFromCSV("courses.csv");
        db.importInstructorsFromCSV("instructors.csv");
        // Create the method importOfferingsFromCSV, then use it in this activity.
        db.importOfferingsFromCSV("offerings.csv");

        List<Course> allCourses = db.getAllCourses();
        for (Course course : allCourses)
            Log.i(TAG, course.toString());

        List<Instructor> allInstructors = db.getAllInstructors();
        for (Instructor instructor : allInstructors)
            Log.i(TAG, instructor.toString());

        // Get all the offerings from the database, then print them out to the Log
        List<Offering> allOfferings = db.getAllOfferings();
        for (Offering offering : allOfferings)
            Log.i(TAG, offering.toString());


    }
}
