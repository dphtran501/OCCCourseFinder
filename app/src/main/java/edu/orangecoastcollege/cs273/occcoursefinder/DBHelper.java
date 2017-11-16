package edu.orangecoastcollege.cs273.occcoursefinder;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A model class to manage the SQLite database used to store <code>Course</code> data,
 * <code>Instructor</code> data, and <code>Offering</code> data.
 *
 * @author Derek Tran
 * @version 1.0
 * @since November 14, 2017
 */
class DBHelper extends SQLiteOpenHelper
{

    private Context mContext;

    //TASK: DEFINE THE DATABASE VERSION AND NAME  (DATABASE CONTAINS MULTIPLE TABLES)
    static final String DATABASE_NAME = "OCC";
    private static final int DATABASE_VERSION = 1;

    //TASK: DEFINE THE FIELDS (COLUMN NAMES) FOR THE COURSES TABLE
    private static final String COURSES_TABLE = "Courses";
    private static final String COURSES_KEY_FIELD_ID = "_id";
    private static final String FIELD_ALPHA = "alpha";
    private static final String FIELD_NUMBER = "number";
    private static final String FIELD_TITLE = "title";

    //TASK: DEFINE THE FIELDS (COLUMN NAMES) FOR THE INSTRUCTORS TABLE
    private static final String INSTRUCTORS_TABLE = "Instructors";
    private static final String INSTRUCTORS_KEY_FIELD_ID = "_id";
    private static final String FIELD_FIRST_NAME = "first_name";
    private static final String FIELD_LAST_NAME = "last_name";
    private static final String FIELD_EMAIL = "email";

    //TASK: DEFINE THE FIELDS (COLUMN NAMES) FOR THE OFFERINGS TABLE
    private static final String OFFERINGS_TABLE = "Offerings";
    private static final String FIELD_CRN = "crn";
    private static final String FIELD_SEMESTER_CODE = "semester_code";
    private static final String FIELD_COURSE_ID = "course_id";
    private static final String FIELD_INSTRUCTOR_ID = "instructor_id";

    /**
     * Instantiates a new <code>DBHelper</code> object with the given context.
     *
     * @param context The activity used to open or create the database.
     */
    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    /**
     * Creates the database tables for the first time.
     *
     * @param database The database.
     */
    @Override
    public void onCreate(SQLiteDatabase database)
    {
        String createQuery = "CREATE TABLE " + COURSES_TABLE + "("
                + COURSES_KEY_FIELD_ID + " INTEGER PRIMARY KEY, "
                + FIELD_ALPHA + " TEXT, "
                + FIELD_NUMBER + " TEXT, "
                + FIELD_TITLE + " TEXT"
                + ")";
        database.execSQL(createQuery);

        createQuery = "CREATE TABLE " + INSTRUCTORS_TABLE + "("
                + INSTRUCTORS_KEY_FIELD_ID + " INTEGER PRIMARY KEY, "
                + FIELD_FIRST_NAME + " TEXT, "
                + FIELD_LAST_NAME + " TEXT, "
                + FIELD_EMAIL + " TEXT"
                + ")";
        database.execSQL(createQuery);

        // Write the query to create the relationship table "Offerings"
        // Make sure to include foreign keys to the Courses and Instructors tables
        createQuery = "CREATE TABLE " + OFFERINGS_TABLE + "("
                + FIELD_CRN + " INTEGER,"
                + FIELD_SEMESTER_CODE + " INTEGER,"
                + FIELD_COURSE_ID + " INTEGER,"
                + FIELD_INSTRUCTOR_ID + " INTEGER,"
                + "FOREIGN KEY (" + FIELD_COURSE_ID + ") REFERENCES " + COURSES_TABLE + "(" + COURSES_KEY_FIELD_ID + "),"
                + "FOREIGN KEY (" + FIELD_INSTRUCTOR_ID + ") REFERENCES " + INSTRUCTORS_TABLE + "(" + INSTRUCTORS_KEY_FIELD_ID + ")"
                + ")";
        database.execSQL(createQuery);

    }

    /**
     * Drops the existing database tables and creates new ones when database is upgraded.
     *
     * @param database   The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        database.execSQL("DROP TABLE IF EXISTS " + COURSES_TABLE);
        database.execSQL("DROP TABLE IF EXISTS " + INSTRUCTORS_TABLE);
        // Drop the Offerings table
        database.execSQL("DROP TABLE IF EXISTS " + OFFERINGS_TABLE);
        onCreate(database);
    }

    //********** COURSE TABLE OPERATIONS:  ADD, GETALL, EDIT, DELETE

    /**
     * Adds a <code>Course</code> to the database.
     *
     * @param course The <code>Course</code> to add to the database.
     */
    public void addCourse(Course course)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FIELD_ALPHA, course.getAlpha());
        values.put(FIELD_NUMBER, course.getNumber());
        values.put(FIELD_TITLE, course.getTitle());

        db.insert(COURSES_TABLE, null, values);

        // CLOSE THE DATABASE CONNECTION
        db.close();
    }

    /**
     * Gets all the <code>Course</code>s in the database.
     *
     * @return A list of all <code>Course</code>s in the database.
     */
    public List<Course> getAllCourses()
    {
        List<Course> coursesList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(COURSES_TABLE,
                new String[]{COURSES_KEY_FIELD_ID, FIELD_ALPHA, FIELD_NUMBER, FIELD_TITLE},
                null, null, null, null, null, null);

        //COLLECT EACH ROW IN THE TABLE
        if (cursor.moveToFirst())
        {
            do
            {
                Course course = new Course(cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3));
                coursesList.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return coursesList;
    }

    /**
     * Deletes a <code>Course</code> in the database.
     *
     * @param course The <code>Course</code> to delete in the database.
     */
    public void deleteCourse(Course course)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        // DELETE THE TABLE ROW
        db.delete(COURSES_TABLE, COURSES_KEY_FIELD_ID + " = ?",
                new String[]{String.valueOf(course.getId())});
        db.close();
    }

    /**
     * Deletes all <code>Course</code>s in the database.
     */
    public void deleteAllCourses()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(COURSES_TABLE, null, null);
        db.close();
    }

    /**
     * Updates a <code>Course</code> record in the database.
     *
     * @param course The <code>Course</code> to update in the database.
     */
    public void updateCourse(Course course)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FIELD_ALPHA, course.getAlpha());
        values.put(FIELD_NUMBER, course.getNumber());
        values.put(FIELD_TITLE, course.getTitle());

        db.update(COURSES_TABLE, values, COURSES_KEY_FIELD_ID + " = ?",
                new String[]{String.valueOf(course.getId())});
        db.close();
    }

    /**
     * Gets a <code>Course</code> in the database.
     *
     * @param id The ID of the <code>Course</code> to get in the database.
     * @return The <code>Course</code> to get in the database.
     */
    public Course getCourse(long id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(COURSES_TABLE,
                new String[]{COURSES_KEY_FIELD_ID, FIELD_ALPHA, FIELD_NUMBER, FIELD_TITLE},
                COURSES_KEY_FIELD_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null, null);

        if (cursor != null) cursor.moveToFirst();

        Course course = new Course(cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3));

        cursor.close();
        db.close();
        return course;
    }

    //********** INSTRUCTOR TABLE OPERATIONS:  ADD, GETALL, EDIT, DELETE

    /**
     * Adds an <code>Instructor</code> to the database.
     *
     * @param instructor The <code>Instructor</code> to add to the database.
     */
    public void addInstructor(Instructor instructor)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FIELD_LAST_NAME, instructor.getLastName());
        values.put(FIELD_FIRST_NAME, instructor.getFirstName());
        values.put(FIELD_EMAIL, instructor.getEmail());

        db.insert(INSTRUCTORS_TABLE, null, values);

        // CLOSE THE DATABASE CONNECTION
        db.close();
    }

    /**
     * Gets all <code>Instructor</code>s in the database.
     *
     * @return A list of all <code>Instructor</code>s in the database.
     */
    public List<Instructor> getAllInstructors()
    {
        List<Instructor> instructorsList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(INSTRUCTORS_TABLE,
                new String[]{INSTRUCTORS_KEY_FIELD_ID, FIELD_LAST_NAME, FIELD_FIRST_NAME, FIELD_EMAIL},
                null, null, null, null, null, null);

        //COLLECT EACH ROW IN THE TABLE
        if (cursor.moveToFirst())
        {
            do
            {
                Instructor instructor = new Instructor(cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3));
                instructorsList.add(instructor);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return instructorsList;
    }

    /**
     * Deletes an <code>Instructor</code> in the database.
     *
     * @param instructor The <code>Instructor</code> to delete in the database.
     */
    public void deleteInstructor(Instructor instructor)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        // DELETE THE TABLE ROW
        db.delete(INSTRUCTORS_TABLE, INSTRUCTORS_KEY_FIELD_ID + " = ?",
                new String[]{String.valueOf(instructor.getId())});
        db.close();
    }

    /**
     * Deletes all <code>Instructor</code>s in the database.
     */
    public void deleteAllInstructors()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(INSTRUCTORS_TABLE, null, null);
        db.close();
    }

    /**
     * Updates an <code>Instructor</code> record in the database.
     *
     * @param instructor The <code>Instructor</code> to update in the database.
     */
    public void updateInstructor(Instructor instructor)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FIELD_FIRST_NAME, instructor.getFirstName());
        values.put(FIELD_LAST_NAME, instructor.getLastName());
        values.put(FIELD_EMAIL, instructor.getEmail());

        db.update(INSTRUCTORS_TABLE, values, INSTRUCTORS_KEY_FIELD_ID + " = ?",
                new String[]{String.valueOf(instructor.getId())});
        db.close();
    }

    /**
     * Gets an <code>Instructor</code> in the database.
     *
     * @param id The ID of the <code>Instructor</code> to get in the database.
     * @return The <code>Instructor</code> to get in the database.
     */
    public Instructor getInstructor(long id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(INSTRUCTORS_TABLE,
                new String[]{INSTRUCTORS_KEY_FIELD_ID, FIELD_LAST_NAME, FIELD_FIRST_NAME, FIELD_EMAIL},
                INSTRUCTORS_KEY_FIELD_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null, null);

        if (cursor != null) cursor.moveToFirst();

        Instructor instructor = new Instructor(cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3));

        cursor.close();
        db.close();
        return instructor;
    }

    //********** OFFERING TABLE OPERATIONS:  ADD, GETALL, EDIT, DELETE
    // Create the following methods: addOffering, getAllOfferings, deleteOffering
    // deleteAllOfferings, updateOffering, and getOffering
    // Use the Courses and Instructors methods above as a guide.

    /**
     * Adds an <code>Offering</code> to the database.
     *
     * @param offering The <code>Offering</code> to add to the database.
     */
    public void addOffering(Offering offering)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FIELD_CRN, offering.getCRN());
        values.put(FIELD_SEMESTER_CODE, offering.getSemesterCode());
        values.put(FIELD_COURSE_ID, offering.getCourse().getId());
        values.put(FIELD_INSTRUCTOR_ID, offering.getInstructor().getId());

        db.insert(OFFERINGS_TABLE, null, values);

        // CLOSE THE DATABASE CONNECTION
        db.close();
    }

    /**
     * Gets all <code>Offering</code>s in the database.
     *
     * @return
     */
    public List<Offering> getAllOfferings()
    {
        List<Offering> offeringsList = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(OFFERINGS_TABLE,
                new String[]{FIELD_CRN, FIELD_SEMESTER_CODE, FIELD_COURSE_ID, FIELD_INSTRUCTOR_ID},
                null, null, null, null, null, null);

        //COLLECT EACH ROW IN THE TABLE
        if (cursor.moveToFirst())
        {
            do
            {
                Offering offering = new Offering(cursor.getInt(0),
                        cursor.getInt(1),
                        getCourse(cursor.getInt(2)),
                        getInstructor(cursor.getInt(3)));
                offeringsList.add(offering);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return offeringsList;
    }

    /**
     * Deletes an <code>Offering</code> in the database.
     *
     * @param offering The <code>Offering</code> to delete in the database.
     */
    public void deleteOffering(Offering offering)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        // DELETE THE TABLE ROW
        db.delete(OFFERINGS_TABLE, FIELD_CRN + " = ?",
                new String[]{String.valueOf(offering.getCRN())});
        db.close();
    }

    /**
     * Deletes all <code>Offering</code>s in the database.
     */
    public void deleteAllOfferings()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(OFFERINGS_TABLE, null, null);
        db.close();
    }

    /**
     * Updates an <code>Offering</code> record in the database.
     *
     * @param offering The <code>Offering</code> to update in the database.
     */
    public void updateOffering(Offering offering)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FIELD_SEMESTER_CODE, offering.getSemesterCode());
        values.put(FIELD_COURSE_ID, offering.getCourse().getId());
        values.put(FIELD_INSTRUCTOR_ID, offering.getInstructor().getId());

        db.update(OFFERINGS_TABLE, values, FIELD_CRN + " = ?",
                new String[]{String.valueOf(offering.getCRN())});
        db.close();
    }

    /**
     * Gets an <code>Offering</code> in the database.
     *
     * @param crn The CRN of the <code>Offering</code> to get in the database.
     * @return The <code>Offering</code> to get in the database.
     */
    public Offering getOffering(int crn)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(OFFERINGS_TABLE,
                new String[]{FIELD_CRN, FIELD_SEMESTER_CODE, FIELD_COURSE_ID, FIELD_INSTRUCTOR_ID},
                FIELD_CRN + "=?", new String[]{String.valueOf(crn)},
                null, null, null, null);

        if (cursor != null) cursor.moveToFirst();

        Offering offering = new Offering(cursor.getInt(0),
                cursor.getInt(1),
                getCourse(cursor.getInt(2)),
                getInstructor(cursor.getInt(3)));

        cursor.close();
        db.close();
        return offering;
    }

    //********** IMPORT FROM CSV OPERATIONS:  Courses, Instructors and Offerings

    /**
     * Imports <code>Course</code>s from a CSV.
     *
     * @param csvFileName The name of the CSV file to import from.
     * @return True
     */
    public boolean importCoursesFromCSV(String csvFileName)
    {
        AssetManager manager = mContext.getAssets();
        InputStream inStream;
        try
        {
            inStream = manager.open(csvFileName);
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));
        String line;
        try
        {
            while ((line = buffer.readLine()) != null)
            {
                String[] fields = line.split(",");
                if (fields.length != 4)
                {
                    Log.d("OCC Course Finder", "Skipping Bad CSV Row: " + Arrays.toString(fields));
                    continue;
                }
                int id = Integer.parseInt(fields[0].trim());
                String alpha = fields[1].trim();
                String number = fields[2].trim();
                String title = fields[3].trim();
                addCourse(new Course(id, alpha, number, title));
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Imports <code>Instructor</code>s from a CSV.
     *
     * @param csvFileName The name of the CSV file to import from.
     * @return True
     */
    public boolean importInstructorsFromCSV(String csvFileName)
    {
        AssetManager am = mContext.getAssets();
        InputStream inStream = null;
        try
        {
            inStream = am.open(csvFileName);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));
        String line;
        try
        {
            while ((line = buffer.readLine()) != null)
            {
                String[] fields = line.split(",");
                if (fields.length != 4)
                {
                    Log.d("OCC Course Finder", "Skipping Bad CSV Row: " + Arrays.toString(fields));
                    continue;
                }
                int id = Integer.parseInt(fields[0].trim());
                String lastName = fields[1].trim();
                String firstName = fields[2].trim();
                String email = fields[3].trim();
                addInstructor(new Instructor(id, lastName, firstName, email));
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Imports <code>Offering</code>s from a CSV.
     *
     * @param csvFileName The name of the CSV file to import from.
     * @return True
     */
    public boolean importOfferingsFromCSV(String csvFileName)
    {
        AssetManager am = mContext.getAssets();
        InputStream inStream = null;
        try
        {
            inStream = am.open(csvFileName);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));
        String line;
        try
        {
            while ((line = buffer.readLine()) != null)
            {
                String[] fields = line.split(",");
                if (fields.length != 4)
                {
                    Log.d("OCC Course Finder", "Skipping Bad CSV Row: " + Arrays.toString(fields));
                    continue;
                }
                int crn = Integer.parseInt(fields[0].trim());
                int semesterCode = Integer.parseInt(fields[1].trim());
                int courseID = Integer.parseInt(fields[2].trim());
                int instructorID = Integer.parseInt(fields[3].trim());
                addOffering(new Offering(crn, semesterCode, getCourse(courseID), getInstructor(instructorID)));
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
