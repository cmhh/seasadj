series {
  start=1986.1
  period=4
  title='femp'
  file='d/femp.dat'
  save=(b1)
  format='datevalue'
  comptype=add
}
 
x11 {
  mode=mult
  sigmalim=(1.8,2.8)
  print=(default f3 qstat)
  save=(c17 d8 d9 d10 d11 d12 d13)
}