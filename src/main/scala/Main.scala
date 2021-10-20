import org.mongodb.scala._
import scala.concurrent._
import org.mongodb.scala.model._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Accumulators._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import io.StdIn._
import scala.io.Source  

import example.Helpers._
import org.bson.BsonValue
import org.bson.codecs.pojo.annotations.BsonId
import scala.collection.SeqMap

 import org.mongodb.scala.model.UpdateOptions
 import org.mongodb.scala.bson.BsonObjectId



object Main extends App {
  def createEntries(collection : MongoCollection[Document]) {
    println("Enter the path of the CSV file")
    var path : String = scala.io.StdIn.readLine()

    val cl: Runtime = Runtime.getRuntime()
    var runCommand: Process = null; 
    val command: String = s"mongoimport --db food --collection recipes --type csv --headerline --drop $path"
    
    try {
      runCommand = cl.exec(command)
    } 
    catch {
        case e: Exception => {
          println("ERROR! Could not read CSV file")
        } 
    }
    println("Import process complete!")
  }

  def createEntry(collection : MongoCollection[Document]) {
    println("Enter the name of the recipe")
    var name : String = scala.io.StdIn.readLine()
    println("Enter the prep time (in minutes)")
    var preptime : Int = scala.io.StdIn.readInt()
    println("Rate how much you like this dish 1-5")
    var rating : Int = scala.io.StdIn.readInt()
    var doc : Document = Document (
      "name" -> name,
      "prepTime" -> preptime,
      "rating" -> rating
    )
    collection.insertOne(doc).printResults()
  }

  def updateEntries(collection : MongoCollection[Document]) {
    println("What is the name of the recipe you would like to update?")
    var name : String = scala.io.StdIn.readLine()
    println("What is the current prep time for this meal?")
    var preptime : Int = scala.io.StdIn.readInt()
    println("What is the current rating for this meal?")
    var rating : Int = scala.io.StdIn.readInt()

    println("Which property would you like to update? " +
      "\n 1.) Name" +
      "\n 2.) Prep time" +
      "\n 3.) Rating")
    var toUpdate : Int = scala.io.StdIn.readInt()
    toUpdate match {
      case 1 => {
        var newName : String = scala.io.StdIn.readLine()
        collection.updateOne(and(equal("name", s"$name")), set("name", s"$newName")).printResults()
      }
      case 2 => {
        var newprep : Int = scala.io.StdIn.readInt()
        collection.updateOne(and(equal("name", s"$name"), equal("prepTime", preptime)), set("prepTime", newprep)).printResults()
      }
      case 3 => {
        var newrating : String = scala.io.StdIn.readLine()
        collection.updateOne(and(equal("name", s"$name"), equal("rating", rating)), set("rating", newrating)).printResults()
      }
      case _ => {
        println("Not an option. Back to main menu...")
      }
    }
  }

  def deleteEntries(collection : MongoCollection[Document]) {
    println("What is the name of the recipe you would like to delete?")
    var name : String = scala.io.StdIn.readLine()
    collection.deleteOne(equal("name", s"$name")).results()
    println("Deleted recipe.")
  }

  def byPrep(collection : MongoCollection[Document]) {
    collection.aggregate(Seq(project(fields(include("name", "prepTime"), excludeId())), Aggregates.sort(ascending("prepTime")), Aggregates.limit(5))).printResults()
  }

  def byRating(collection : MongoCollection[Document]) {
    collection.aggregate(Seq(project(fields(include("name", "rating"), excludeId())), Aggregates.sort(descending("rating")), Aggregates.limit(5))).printResults()
  }

  println("\n\nWelcome to Hungry Helper.")

  println("\nStarting MongoClient")
  val client: MongoClient = MongoClient()

  println("\nOpening the food database...")
  val database: MongoDatabase = client.getDatabase("food")

  println("Opening the recipes collection...")
  val collection: MongoCollection[Document] = database.getCollection("recipes")

  // UI + CRUD options
  var running : Boolean = true
  while(running) {
    println("\n\nEnter an integer from the menu of options: " +
      "\n 1.) Create new entries with a CSV file " +
      "\n 2.) Create a single entry by filling out fields for a document" +
      "\n 3.) Update fields of existing entries" +
      "\n 4.) Delete existing entries" +
      "\n 5.) See top 5 fastest recipes" +
      "\n 6.) See top 5 recipes by rating" +
      "\n 7.) Exit program" );
    var userInput : Int = readInt()
    userInput match {
      case 1 => createEntries(collection)
      case 2 => createEntry(collection)
      case 3 => updateEntries(collection)
      case 4 => deleteEntries(collection)
      case 5 => byPrep(collection)
      case 6 => byRating(collection)
      case 7 => running = false
      case _ => println("Please enter a valid option.")
    } 
  }

  println("Closing Mongo connection...")
  client.close()

  println("Goodbye!")
}
