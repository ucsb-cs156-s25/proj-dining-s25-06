package edu.ucsb.cs156.dining.controllers;

import edu.ucsb.cs156.dining.models.Entree;
import edu.ucsb.cs156.dining.entities.MenuItem;
import edu.ucsb.cs156.dining.repositories.MenuItemRepository;
import edu.ucsb.cs156.dining.controllers.UCSBDiningMenuItemsController;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;
import static org.mockito.Mockito.verify;

import edu.ucsb.cs156.dining.ControllerTestCase;
import edu.ucsb.cs156.dining.config.SecurityConfig;
import edu.ucsb.cs156.dining.services.UCSBDiningMenuItemsService;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.mockito.Mockito.times;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

@WebMvcTest(value = UCSBDiningMenuItemsController.class)
@Import(SecurityConfig.class)
@AutoConfigureDataJpa
public class UCSBDiningMenuItemsControllerTests extends ControllerTestCase {

  @Autowired private MockMvc mockMvc;

  @MockBean private UCSBDiningMenuItemsService ucsbDiningMenuItemsService;

  @Autowired private ObjectMapper objectMapper;

  @MockBean
  MenuItemRepository menuItemRepository;

  private static final String NAME = "NAME";
  private static final String STATION = "STATION";


  @WithMockUser(roles = { "USER" })
  @Test
  public void meal_item_created_and_found() throws Exception {
    String dateTime = "2023-10-11";
    String diningCommonCode = "portola";
    String mealCode = "dinner";
    String name = "Spicy Tuna Roll";
    String station = "International";

    Entree entree = new Entree(name, station);
        List<Entree> entrees = new ArrayList<>();
        entrees.add(entree);

    when(ucsbDiningMenuItemsService.get(dateTime, diningCommonCode, mealCode))
            .thenReturn(entrees);

    when(menuItemRepository.findByDiningCommonsCodeAndMealCodeAndNameAndStation(diningCommonCode, mealCode, name, station))
        .thenReturn(Optional.empty());

    when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(invocation -> {
      MenuItem menuItem = invocation.getArgument(0);
      menuItem.setId(1L);
      return menuItem;
    });

    MvcResult result = mockMvc.perform(get("/api/diningcommons/2023-10-11/portola/dinner")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].name").value(name))
          .andReturn();

    String responseBody = result.getResponse().getContentAsString();

    List<MenuItem> menuItems = objectMapper.readValue(responseBody, new TypeReference<List<MenuItem>>(){});
  
    for(MenuItem item : menuItems){
        menuItemRepository.save(item);

      when(menuItemRepository.findByDiningCommonsCodeAndMealCodeAndNameAndStation(diningCommonCode, mealCode, name, station))
            .thenReturn(Optional.of(new MenuItem(1L, diningCommonCode, mealCode, name, station, null)));

      assertEquals(item.getDiningCommonsCode(), "portola");
      assertEquals(item.getMealCode(), "dinner");
      assertEquals(item.getStation(), "International");
    }

    Optional<MenuItem> found = menuItemRepository.findByDiningCommonsCodeAndMealCodeAndNameAndStation(diningCommonCode, mealCode, name, station);
    assertTrue(found.isPresent());
  }
@WithMockUser(roles = { "USER" })
@Test
public void test_get_menu_item_by_id_returns_expected_item() throws Exception {
    // Arrange
    Long id = 1L;
    MenuItem menuItem = new MenuItem();
    menuItem.setId(id);
    menuItem.setName("Chicken Alfredo");
    menuItem.setStation("Entrees");
    menuItem.setDiningCommonsCode("portola");
    menuItem.setMealCode("lunch");

    when(menuItemRepository.findById(id)).thenReturn(Optional.of(menuItem));

    // Act & Assert
    mockMvc.perform(get("/api/diningcommons/menuitem")
            .param("id", id.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Chicken Alfredo"))
            .andExpect(jsonPath("$.station").value("Entrees"))
            .andExpect(jsonPath("$.diningCommonsCode").value("portola"))
            .andExpect(jsonPath("$.mealCode").value("lunch"));
}
@WithMockUser(roles = { "USER" })
@Test
public void test_get_menu_item_by_id_when_not_found_returns_404() throws Exception {
    Long id = 999L;
    when(menuItemRepository.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/diningcommons/menuitem")
            .param("id", id.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.type").value("EntityNotFoundException"))
            .andExpect(jsonPath("$.message").value("MenuItem with id 999 not found"));
}
}