# -*- coding: utf-8 -*-
"""
Created on Wed Jan 14 21:23:18 2015

@author: Geoffrey
"""

import pandas as pd
import numpy as np

col = ["CAT_PLAYER", "CAT_SENSOR", "CAT_OBJECT", "CAT_WEAPON",
"CAT_COLLECTABLE", "CAT_ATTACHEDOBJECT", "CAT_SCENERY"]
ind = ["MASK_PLAYER", "MASK_SENSOR", "MASK_OBJECT", "MASK_WEAPON",
"MASK_COLLECTABLE", "MASK_ATTACHEDOBJECT", "MASK_SCENERY"]
df = pd.DataFrame(columns = col, index = ind)
