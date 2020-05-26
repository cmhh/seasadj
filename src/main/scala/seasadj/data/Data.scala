package org.cmhh.seasadj

object data {
  /**
   * Box and Jenkins airline passengers data.
   */
  val airpassengers =  TimeSeries(
    data = Vector(
      112, 118, 132, 129, 121, 135, 148, 148, 136, 119, 104, 118,
      115, 126, 141, 135, 125, 149, 170, 170, 158, 133, 114, 140,
      145, 150, 178, 163, 172, 178, 199, 199, 184, 162, 146, 166,
      171, 180, 193, 181, 183, 218, 230, 242, 209, 191, 172, 194,
      196, 196, 236, 235, 229, 243, 264, 272, 237, 211, 180, 201,
      204, 188, 235, 227, 234, 264, 302, 293, 259, 229, 203, 229,
      242, 233, 267, 269, 270, 315, 364, 347, 312, 274, 237, 278,
      284, 277, 317, 313, 318, 374, 413, 405, 355, 306, 271, 306,
      315, 301, 356, 348, 355, 422, 465, 467, 404, 347, 305, 336,
      340, 318, 362, 348, 363, 435, 491, 505, 404, 359, 310, 337,
      360, 342, 406, 396, 420, 472, 548, 559, 463, 407, 362, 405,
      417, 391, 419, 461, 472, 535, 622, 606, 508, 461, 390, 432
    ),
    start = Month(1949,1),
    frequency = Frequency.MONTHLY
  )

  /**
   * Example data from Stats NZ Household Labour Force Survey
   */
  object hlfs {
    /**
     * Male employed.
     */
    val memp = TimeSeries(
      data = Vector(
        953.8,944.0,936.2,938.7,946.1,933.7,931.1,932.4,915.8,899.3,
        879.3,889.3,879.3,864.3,858.9,874.0,869.6,863.2,856.6,863.9,
        852.6,843.9,832.4,841.2,845.7,844.0,835.1,849.4,854.0,859.2,
        856.7,879.2,881.9,890.7,896.2,916.4,929.6,934.5,936.7,958.1,
        957.7,957.4,959.4,971.6,967.8,964.3,960.7,973.1,966.6,952.3,
        944.0,954.4,958.7,952.7,955.9,984.1,979.3,970.7,976.6,998.4,
        997.4,989.0,998.8,1019.2,1036.2,1027.8,1027.3,1048.0,1050.2,
        1045.5,1055.9,1072.2,1081.2,1088.0,1091.2,1121.3,1114.6,1106.5,
        1118.8,1141.1,1139.8,1141.8,1139.6,1161.2,1158.9,1155.7,1157.9,
        1170.8,1151.0,1154.9,1145.9,1173.3,1142.6,1143.7,1117.2,1138.9,
        1140.8,1134.9,1146.7,1152.7,1157.5,1150.6,1153.1,1163.5,1164.0,
        1150.5,1137.9,1144.3,1160.0,1159.3,1170.3,1197.3,1204.7,1207.7,
        1205.4,1232.8,1243.1,1239.6,1224.0,1252.5,1263.2,1287.9,1298.1,
        1320.5,1338.5,1326.2,1342.1,1361.2,1363.9,1356.3,1361.7,1365.2,
        1374.1,1370.5,1376.2,1393.7
      ),
      start = Quarter(1986, 1),
      frequency = Frequency.QUARTERLY
    )

    /**
     * Female employed.
     */
    val femp = TimeSeries(
      data = Vector(
        672.2,671.7,674.3,682.8,683.2,685.5,685.4,697.6,682.9,676.9,
        667.7,675.5,655.5,648.9,650.8,662.4,664.2,677.5,669.2,674.4,
        666.6,667.6,663.4,668.9,662.7,672.6,666.9,679.1,671.5,674.5,
        686.9,697.0,699.8,707.2,716.2,736.7,732.1,741.0,750.8,762.2,
        764.6,777.8,787.4,784.4,778.0,783.1,784.1,789.4,779.9,777.5,
        786.6,792.5,795.6,796.6,802.6,816.5,807.5,802.8,822.6,838.4,
        829.7,840.6,839.3,864.8,859.3,865.0,868.5,886.1,878.2,885.6,
        907.2,918.4,910.8,906.7,931.2,960.8,947.2,953.9,969.9,977.3,
        979.8,988.6,984.1,991.1,999.5,1005.4,997.4,1028.3,996.0,1018.0,
        1024.9,1039.6,1014.4,1006.5,1008.3,1016.5,1008.7,1008.7,1010.1,
        1023.9,1027.9,1030.5,1025.7,1043.9,1035.7,1038.6,1036.5,1026.3,
        1044.1,1042.7,1056.2,1075.0,1079.6,1070.7,1087.3,1118.1,1114.5,
        1104.0,1099.5,1128.3,1136.7,1155.8,1161.6,1190.6,1190.2,1182.9,
        1207.3,1228.7,1228.3,1230.8,1248.1,1240.2,1254.0,1256.5,1261.7,
        1272.1
      ),
      start = Quarter(1986, 1),
      frequency = Frequency.QUARTERLY
    )

    /**
     * Male unemployed.
     */
    val munemp = TimeSeries(
      data = Vector(
        37.2,35.2,35.0,36.4,37.8,38.9,39.9,41.5,49.3,51.8,59.7,61.9,
        70.8,72.2,70.2,66.9,71.6,78.0,78.6,87.9,99.4,102.3,112.2,114.3,
        114.6,105.8,107.6,106.1,107.3,101.4,95.9,98.0,97.4,87.6,81.8,
        78.6,71.4,61.6,62.5,63.8,67.8,60.4,64.6,66.8,69.3,70.2,70.7,73.8,
        79.4,80.5,81.7,82.4,85.2,77.5,74.2,66.8,70.7,64.5,64.6,62.7,60.6,
        57.7,54.8,59.3,59.9,53.4,58.5,52.6,55.0,48.4,45.6,47.3,44.3,40.1,
        39.4,38.1,46.4,42.0,38.6,36.6,47.7,38.2,41.9,40.9,46.4,37.6,37.9,
        39.1,46.2,44.5,48.0,50.6,65.6,64.6,73.9,77.9,71.7,77.4,66.0,71.6,
        73.5,69.9,67.4,73.7,78.5,71.4,78.5,72.4,64.8,64.3,66.0,62.7,66.2,
        55.8,56.2,62.5,68.6,59.6,66.1,61.4,67.4,60.5,63.2,66.5,61.7,62.8,
        56.4,56.7,57.7,59.0,54.6,64.5,57.0,50.9,52.7,56.4
      ),
      start = Quarter(1986, 1),
      frequency = Frequency.QUARTERLY
    )

    /**
     * Female unemployed.
     */
    val funemp = TimeSeries(
      data = Vector(
        37.8,34.7,33.1,33.4,34.9,32.4,29.0,31.2,38.1,39.7,43.9,40.7,51.7,
        49.6,47.6,51.2,52.9,49.1,53.2,60.5,73.9,71.8,72.1,70.6,80.3,68.7,
        66.8,73.6,72.2,69.4,62.6,64.6,72.8,59.5,54.3,56.2,56.1,51.3,46.8,
        52.6,55.2,52.7,52.7,47.6,62.3,56.0,57.2,55.1,67.5,66.7,59.3,64.5,
        64.1,59.5,54.3,53.0,59.9,54.4,47.4,46.4,51.7,46.3,46.8,48.1,54.0,
        50.9,49.3,46.6,54.3,47.7,43.7,47.6,53.1,44.3,39.4,39.6,45.6,37.6,
        40.9,42.5,51.4,40.6,40.6,40.2,49.1,40.4,38.8,34.3,47.4,39.1,40.6,
        47.9,56.8,62.1,62.2,67.9,72.9,68.2,68.9,68.2,74.5,65.3,67.5,64.0,
        77.9,72.3,75.0,72.0,76.9,70.9,68.4,70.5,75.1,66.5,68.4,71.8,74.3,
        72.6,68.5,61.1,71.9,65.2,61.4,70.4,76.2,59.5,65.3,63.0,67.8,60.0,
        49.5,54.2,64.5,54.9,56.2,55.6
      ),
      start = Quarter(1986, 1),
      frequency = Frequency.QUARTERLY
    )

    /**
     * Male not in the labour force.
     */
    val mnilf = TimeSeries(
      data = Vector(
        241.1,252.9,261.0,260.3,255.5,268.7,272.1,272.6,283.9,298.4,310.9,
        300.6,303.1,316.6,325.0,317.3,321.6,324.6,333.6,322.1,327.4,337.1,
        341.6,335.0,334.9,349.0,358.7,349.7,348.6,353.0,364.0,344.3,347.8,
        353.2,357.5,345.9,346.1,355.9,357.6,341.6,345.1,358.0,356.0,347.3,
        354.3,360.2,365.9,354.1,358.6,373.5,381.9,373.4,368.6,383.0,384.5,
        367.0,370.8,386.6,382.2,365.7,371.4,384.6,382.1,365.0,356.3,378.6,
        381.1,375.5,381.4,401.0,400.3,389.9,391.4,394.2,396.7,374.4,379.2,
        396.2,392.1,378.6,376.0,390.7,393.6,378.2,380.1,394.9,394.9,385.3,
        402.2,402.5,410.5,384.7,405.4,409.7,431.5,411.7,421.8,425.3,427.9,
        421.4,419.1,431.7,433.6,420.7,418.7,441.0,448.5,452.5,449.5,454.9,
        447.2,431.0,429.1,444.9,455.6,432.6,427.7,450.1,468.7,456.4,451.6,
        451.4,447.1,431.3,428.4,447.8,445.2,433.8,439.5,453.3,459.4,456.0,
        465.3,483.1,482.6,471.0
      ),
      start = Quarter(1986, 1),
      frequency = Frequency.QUARTERLY
    )

    /**
     * Female not in the labour force.
     */
    val fnilf = TimeSeries(
      data = Vector(
        587.0,591.7,592.1,587.7,590.4,593.0,598.9,588.4,599.3,604.6,610.6,
        608.8,620.2,629.5,631.1,620.2,621.7,615.1,622.5,615.4,615.2,619.7,
        626.7,627.1,627.7,632.1,642.3,627.5,641.4,644.6,642.3,635.2,629.9,
        640.1,640.3,623.6,634.6,635.0,634.3,623.6,625.5,619.9,614.9,629.5,
        627.4,632.8,634.3,636.0,637.7,643.7,644.8,637.9,638.8,644.4,646.0,
        637.9,643.8,656.1,646.0,635.5,643.8,640.0,645.1,626.5,634.8,639.3,
        644.9,639.5,649.9,657.5,647.6,641.2,651.6,670.7,657.0,634.6,649.4,
        656.1,642.5,641.4,638.0,647.4,657.3,656.9,645.3,651.5,664.5,643.3,
        667.3,656.7,651.8,634.6,656.1,663.2,666.4,659.1,668.1,676.8,678.6,
        670.8,664.9,674.1,679.3,668.8,666.6,671.7,673.9,691.6,673.7,685.2,
        678.7,664.5,663.1,687.3,675.7,650.6,661.3,681.6,698.3,687.5,679.7,
        678.0,684.3,656.5,661.5,693.6,670.2,659.6,664.2,676.9,677.1,690.2,
        676.7,691.1,690.8,690.0
      ),
      start = Quarter(1986, 1),
      frequency = Frequency.QUARTERLY
    )
  }
}
