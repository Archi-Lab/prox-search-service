package io.archilab.prox.searchservice.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class ProjectRepositoryCustomImpl implements ProjectRepositoryCustom{
  

  @PersistenceContext
  private EntityManager entityManager;


  @Override
  public List<Project> findAllSearchedProjects(Pageable pageable, String searchText) throws Exception {
    
    int pageNumber = pageable.getPageNumber();
    int pageSize = pageable.getPageSize();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    
    countQuery.select(criteriaBuilder
        .count(countQuery.from(Project.class)));
      Long count = entityManager.createQuery(countQuery)
        .getSingleResult();

      CriteriaQuery<Project> criteriaQuery = criteriaBuilder
        .createQuery(Project.class);
      Root<Project> from = criteriaQuery.from(Project.class);
      CriteriaQuery<Project> select = criteriaQuery.select(from);

      TypedQuery<Project> typedQuery = entityManager.createQuery(select);
      
      typedQuery.setFirstResult(pageNumber);
      typedQuery.setMaxResults(pageSize);
      
      if(pageNumber < count.intValue())
      {
        List<Project> fooList = typedQuery.getResultList();
        
//        int toIndex = Math.min(startItem + pageSize, books.size());
//        list = books.subList(startItem, toIndex);

        Page<Project> sssssa
          = new PageImpl<Project>(fooList, pageable , count);  // PageRequest.of(pageNumber, pageSize)
        
        return fooList;
      }
      else
      {
        throw new Exception("page size too big");
      }
      
    
    
    
//    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//    CriteriaQuery<Project> query = cb.createQuery(Project.class);
//    Root<Project> project = query.from(Project.class);
//    
//    List<Predicate> predicates = new ArrayList<Predicate>();
//    
//    Predicate[] predArray = new Predicate[predicates.size()];
//    predicates.toArray(predArray);
//    
//    query
//    .select(project.get("id"))
//    .where(predArray);

      
    
  }
  
  @Override
  public Page<String> findAllSearchedProjects2(Pageable pageable, String searchText) throws Exception {
    
    int pageNumber = pageable.getPageNumber();
    int pageSize = pageable.getPageSize();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    
    countQuery.select(criteriaBuilder
        .count(countQuery.from(Project.class)));
      Long count = entityManager.createQuery(countQuery)
        .getSingleResult();

      CriteriaQuery<Project> criteriaQuery = criteriaBuilder
        .createQuery(Project.class);
      Root<Project> from = criteriaQuery.from(Project.class);
      CriteriaQuery<Project> select = criteriaQuery.select(from);

      TypedQuery<Project> typedQuery = entityManager.createQuery(select);
      
      typedQuery.setFirstResult(pageNumber);
      typedQuery.setMaxResults(pageSize);
      
      if(pageNumber < count.intValue())
      {
        List<Project> fooList = typedQuery.getResultList();
        List<String> retList = new ArrayList<>();
        
        for(int i=0;i<fooList.size();i++)
        {
          Project pt = fooList.get(i);
          String rest = pt.getId().toString();
          retList.add(rest);
        }
        
//        int toIndex = Math.min(startItem + pageSize, books.size());
//        list = books.subList(startItem, toIndex);

        Page<String> sssssa
          = new PageImpl<String>(retList, pageable , count);  // PageRequest.of(pageNumber, pageSize)
        
        return sssssa;
      }
      else
      {
        throw new Exception("page size too big");
      }
      
      
  }

}
