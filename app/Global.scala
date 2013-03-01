
import play.GlobalSettings
import data.MongoData

class Global extends GlobalSettings {


  override def onStart(app :play.Application) : Unit = {
    super.beforeStart(app)
    
    MongoData.initalize()
  }

}
