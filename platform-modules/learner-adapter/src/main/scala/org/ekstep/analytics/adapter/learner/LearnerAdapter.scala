package org.ekstep.analytics.adapter.learner

import com.websudos.phantom.connectors._
import org.ekstep.analytics.adapter.model.LearnerActivityDAO

/**
 * @author Santhosh
 */

object Defaults {
    val connector = ContactPoint.local.keySpace("learner_db");
    implicit val session = connector.session
}

class LearnerDatabase(val keyspace: KeySpaceDef) extends com.websudos.phantom.db.DatabaseImpl(keyspace) {
    object LearnerActivitySummary extends LearnerActivityDAO with keyspace.Connector
}

object LearnerAdapter extends LearnerDatabase(Defaults.connector)