package repository.solr;

@org.springframework.stereotype.Repository
public interface PushNotificationSolrRepository
    extends org.springframework.data.solr.repository.SolrCrudRepository<
        models.PushNotification, Long> {}
