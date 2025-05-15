const diningCommonsFixtures = {
  oneCommons: {
    name: "Carrillo",
    code: "carrillo",
    hasSackMeal: false,
    hasTakeoutMeal: false,
    hasDiningCam: true,
  },
  oneCommonsDiningCamFalse: {
    name: "Carrillo",
    code: "carrillo",
    hasSackMeal: false,
    hasTakeoutMeal: false,
    hasDiningCam: false,
  },
  fourCommons: [
    {
      name: "Carrillo",
      code: "carrillo",
      hasSackMeal: false,
      hasTakeoutMeal: false,
      hasDiningCam: true,
    },
    {
      name: "De La Guerra",
      code: "de-la-guerra",
      hasSackMeal: false,
      hasTakeoutMeal: false,
      hasDiningCam: true,
    },
    {
      name: "Ortega",
      code: "ortega",
      hasSackMeal: true,
      hasTakeoutMeal: true,
      hasDiningCam: true,
    },
    {
      name: "Portola",
      code: "portola",
      hasSackMeal: true,
      hasTakeoutMeal: true,
      hasDiningCam: true,
    },
  ],
};

export { diningCommonsFixtures };
