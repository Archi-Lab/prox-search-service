//package io.archilab.prox.searchservice;
//
//import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.ResultActions;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import io.archilab.prox.searchservice.controller.SearchController;
//import io.archilab.prox.searchservice.project.ProjectRepository;
//
//@RunWith(SpringRunner.class)
////@SpringBootTest
//@SpringBootTest(webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT)
////@WebMvcTest(SearchController.class)
//public class DataControllerTest {
//  
//
//  Logger log = LoggerFactory.getLogger(DataControllerTest.class);
//
//  @Autowired
//  private WebApplicationContext context;
//
//  private MockMvc mockMvc;
//
//  @Before
//  public void setup() {
//    mockMvc = MockMvcBuilders
//        .webAppContextSetup(context)
////        .apply(springSecurity())
//        .build();
//  }
//
//  
//  @Test
////  @WithMockUser("professor")
//  public void fullSearchTest() {
//    
//    try {
//      MvcResult  res = mockMvc.perform(get("/search/projects?page=0&size=10&searchText=hallo")).andReturn();
//
//      String content = res.getResponse().getContentAsString();
//      log.info(content);
//    } catch (Exception e1) {
//      log.error("error exc");
//      
//      e1.printStackTrace();
//    }
//
//    
//
//  }
//}
