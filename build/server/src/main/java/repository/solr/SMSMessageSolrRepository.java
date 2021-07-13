package repository.solr;

@org.springframework.stereotype.Repository
public interface SMSMessageSolrRepository
    extends org.springframework.data.solr.repository.SolrCrudRepository<models.SMSMessage, Long> {}
